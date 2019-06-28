package dd.kms.zenodot.parsers;

import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.common.ConstructorScanner;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.EvaluationMode;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataProviders.ExecutableDataProvider;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Parses expressions of the form
 * <ul>
 *     <li>{@code new <class>(<arguments>)},</li>
 *     <li>{@code new <class>[<size>]}, and</li>
 *     <li>{@code new <class>[]{<elements>}}.</li>
 * </ul>
 * The (ignored) context of the parser is {@code this}.
 */
public class ConstructorParser extends AbstractParserWithObjectTail<ObjectInfo>
{
	public ConstructorParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ParseOutcome parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		Token operatorToken = tokenStream.readKeyWordUnchecked();
		if (operatorToken == null) {
			log(LogLevel.ERROR, "'new' operator expected");
			return ParseOutcomes.createParseError(startPosition, "Expected operator 'new'", ErrorPriority.WRONG_PARSER);
		}
		if (operatorToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available");
			return CompletionSuggestions.none(tokenStream.getPosition());
		}
		if (!operatorToken.getValue().equals("new")) {
			log(LogLevel.ERROR, "'new' operator expected");
			return ParseOutcomes.createParseError(startPosition, "Expected operator 'new'", ErrorPriority.WRONG_PARSER);
		}

		log(LogLevel.INFO, "parsing class at " + tokenStream);
		ParseOutcome classParseOutcome = ParseUtils.parseClass(tokenStream, parserToolbox);
		ParseOutcomeType parseOutcomeType = classParseOutcome.getOutcomeType();
		log(LogLevel.INFO, "parse outcome: " + parseOutcomeType);

		Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(classParseOutcome, ParseExpectation.CLASS, ErrorPriority.RIGHT_PARSER);
		if (parseOutcomeForPropagation.isPresent()) {
			return parseOutcomeForPropagation.get();
		}
		ClassParseResult parseResult = (ClassParseResult) classParseOutcome;
		int parsedToPosition = parseResult.getPosition();
		TypeInfo type = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		char nextChar = tokenStream.peekCharacter();
		if (nextChar == '(') {
			return parseObjectConstructor(tokenStream, startPosition, type);
		} else if (nextChar == '[') {
			return parseArrayConstructor(tokenStream, startPosition, type);
		} else {
			log(LogLevel.ERROR, "missing '(' at " + tokenStream);
			return ParseOutcomes.createParseError(tokenStream.getPosition(), "Expected opening parenthesis '('", ErrorPriority.WRONG_PARSER);
		}
	}

	private ParseOutcome parseObjectConstructor(TokenStream tokenStream, int startPosition, TypeInfo constructorType) {
		Class<?> constructorClass = constructorType.getRawType();
		if (constructorClass.getEnclosingClass() != null && !Modifier.isStatic(constructorClass.getModifiers())) {
			log(LogLevel.ERROR, "cannot instantiate non-static inner class");
			return ParseOutcomes.createParseError(tokenStream.getPosition(), "Cannot instantiate inner class '" + constructorClass.getName() + "'", ErrorPriority.RIGHT_PARSER);
		}
		List<ExecutableInfo> constructorInfos = getConstructorInfos(constructorType);

		log(LogLevel.INFO, "parsing constructor arguments");
		ExecutableDataProvider executableDataProvider = parserToolbox.getExecutableDataProvider();
		List<ParseOutcome> argumentParseOutcomes = executableDataProvider.parseExecutableArguments(tokenStream, constructorInfos);

		if (argumentParseOutcomes.isEmpty()) {
			log(LogLevel.INFO, "no arguments found");
		} else {
			int lastArgumentIndex = argumentParseOutcomes.size() - 1;
			ParseOutcome lastArgumentParseOutcome = argumentParseOutcomes.get(lastArgumentIndex);
			ParseOutcomeType lastArgumentParseOutcomeType = lastArgumentParseOutcome.getOutcomeType();
			log(LogLevel.INFO, "parse outcome: " + lastArgumentParseOutcomeType);

			if (lastArgumentParseOutcome.getOutcomeType() == ParseOutcomeType.COMPLETION_SUGGESTIONS) {
				CompletionSuggestions argumentSuggestions = (CompletionSuggestions) lastArgumentParseOutcome;
				// add argument information
				if (argumentSuggestions.getExecutableArgumentInfo().isPresent()) {
					// information has already been added for an executable used in a subexpression, which is more relevant
					return argumentSuggestions;
				}
				List<ObjectInfo> previousArgumentInfos = argumentParseOutcomes.subList(0, lastArgumentIndex).stream()
					.map(ObjectParseResult.class::cast)
					.map(ObjectParseResult::getObjectInfo)
					.collect(Collectors.toList());
				ExecutableArgumentInfo executableArgumentInfo = executableDataProvider.createExecutableArgumentInfo(constructorInfos, previousArgumentInfos);
				return new CompletionSuggestions(argumentSuggestions.getPosition(), argumentSuggestions.getRatedSuggestions(), Optional.of(executableArgumentInfo));
			}

			Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(lastArgumentParseOutcome, ParseExpectation.OBJECT, ErrorPriority.RIGHT_PARSER);
			if (parseOutcomeForPropagation.isPresent()) {
				return parseOutcomeForPropagation.get();
			}
		}

		List<ObjectInfo> argumentInfos = argumentParseOutcomes.stream()
			.map(ObjectParseResult.class::cast)
			.map(ObjectParseResult::getObjectInfo)
			.collect(Collectors.toList());
		List<ExecutableInfo> bestMatchingConstructorInfos = parserToolbox.getExecutableDataProvider().getBestMatchingExecutableInfos(constructorInfos, argumentInfos);

		switch (bestMatchingConstructorInfos.size()) {
			case 0:
				log(LogLevel.ERROR, "no matching constructor found");
				return ParseOutcomes.createParseError(tokenStream.getPosition(), "No constructor matches the given arguments", ErrorPriority.RIGHT_PARSER);
			case 1: {
				ExecutableInfo bestMatchingConstructorInfo = bestMatchingConstructorInfos.get(0);
				ObjectInfo constructorReturnInfo;
				try {
					constructorReturnInfo = parserToolbox.getObjectInfoProvider().getExecutableReturnInfo(null, bestMatchingConstructorInfo, argumentInfos);
					log(LogLevel.SUCCESS, "found unique matching constructor");
				} catch (Exception e) {
					log(LogLevel.ERROR, "caught exception: " + e.getMessage());
					return ParseOutcomes.createParseError(startPosition, "Exception during constructor evaluation", ErrorPriority.EVALUATION_EXCEPTION, e);
				}
				int position = tokenStream.getPosition();
				return isCompile()
						? compileObjectConstructorParseResult(bestMatchingConstructorInfo, argumentParseOutcomes, position, constructorReturnInfo)
						: ParseOutcomes.createObjectParseResult(position, constructorReturnInfo);
			}
			default: {
				String error = "Ambiguous constructor call. Possible candidates are:\n"
								+ bestMatchingConstructorInfos.stream().map(ConstructorParser::formatConstructorInfo).collect(Collectors.joining("\n"));
				log(LogLevel.ERROR, error);
				return ParseOutcomes.createAmbiguousParseResult(tokenStream.getPosition(), error);
			}
		}
	}

	private ParseOutcome parseArrayConstructor(TokenStream tokenStream, int startPosition, TypeInfo componentType) {
		// TODO: currently, only 1d arrays are supported
		ParseOutcome arraySizeParseOutcome = parseArraySize(tokenStream);
		if (arraySizeParseOutcome == null) {
			// array constructor with initializer list (e.g., "new int[] { 1, 2, 3 }")
			log(LogLevel.INFO, "parsing array elements at " + tokenStream);
			List<ParseOutcome> elementParseOutcomes = parseArrayElements(tokenStream, ParseExpectationBuilder.expectObject().allowedType(componentType).build());

			if (elementParseOutcomes.isEmpty()) {
				log(LogLevel.INFO, "detected empty array");
			} else {
				ParseOutcome lastArgumentParseOutcome = elementParseOutcomes.get(elementParseOutcomes.size()-1);
				ParseOutcomeType lastArgumentParseOutcomeType = lastArgumentParseOutcome.getOutcomeType();
				log(LogLevel.INFO, "parse outcome: " + lastArgumentParseOutcomeType);

				Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(lastArgumentParseOutcome, ParseExpectation.OBJECT, ErrorPriority.RIGHT_PARSER);
				if (parseOutcomeForPropagation.isPresent()) {
					return parseOutcomeForPropagation.get();
				}
			}

			List<ObjectInfo> elementInfos = elementParseOutcomes.stream().map(ObjectParseResult.class::cast).map(ObjectParseResult::getObjectInfo).collect(Collectors.toList());
			ObjectInfo arrayInfo = parserToolbox.getObjectInfoProvider().getArrayInfo(componentType, elementInfos);
			log(LogLevel.SUCCESS, "detected valid array construction with initializer list");
			int position = tokenStream.getPosition();
			return isCompile()
					? compileArrayConstructorWithInitializerListParseResult(componentType, elementParseOutcomes, position, arrayInfo)
					: ParseOutcomes.createObjectParseResult(position, arrayInfo);
		} else {
			// array constructor with default initialization (e.g., "new int[3]")
			Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(arraySizeParseOutcome, ParseExpectation.OBJECT, ErrorPriority.RIGHT_PARSER);
			if (parseOutcomeForPropagation.isPresent()) {
				return parseOutcomeForPropagation.get();
			}
			ObjectParseResult sizeParseResult = (ObjectParseResult) arraySizeParseOutcome;
			int parsedToPosition = sizeParseResult.getPosition();
			ObjectInfo sizeInfo = sizeParseResult.getObjectInfo();
			ObjectInfo arrayInfo;
			try {
				arrayInfo = parserToolbox.getObjectInfoProvider().getArrayInfo(componentType, sizeInfo);
				log(LogLevel.SUCCESS, "detected valid array construction with null initialization");
			} catch (ClassCastException | NegativeArraySizeException e) {
				log(LogLevel.ERROR, "caught exception: " + e.getMessage());
				return ParseOutcomes.createParseError(startPosition, e.getClass().getSimpleName() + " during array construction", ErrorPriority.EVALUATION_EXCEPTION, e);
			}
			tokenStream.moveTo(parsedToPosition);
			int position = tokenStream.getPosition();
			return isCompile()
					? compileArrayConstructorWithDefaultInitialization(componentType, sizeParseResult, position, arrayInfo)
					: ParseOutcomes.createObjectParseResult(position, arrayInfo);
		}
	}

	// returns null if no size is specified (e.g. in "new int[] { 1, 2, 3 }")
	private ParseOutcome parseArraySize(TokenStream tokenStream) {
		log(LogLevel.INFO, "parsing array size");

		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals("[");

		if (tokenStream.peekCharacter() == ']') {
			tokenStream.readCharacterUnchecked();
			return null;
		}

		ParseExpectation expectation = ParseExpectationBuilder.expectObject().allowedType(InfoProvider.createTypeInfo(int.class)).build();
		ParseOutcome arraySizeParseOutcome = parserToolbox.getExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), expectation);

		Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(arraySizeParseOutcome, expectation, ErrorPriority.RIGHT_PARSER);
		if (parseOutcomeForPropagation.isPresent()) {
			return parseOutcomeForPropagation.get();
		}

		ObjectParseResult sizeParseResult = ((ObjectParseResult) arraySizeParseOutcome);
		int parsedToPosition = sizeParseResult.getPosition();

		tokenStream.moveTo(parsedToPosition);
		characterToken = tokenStream.readCharacterUnchecked();

		if (characterToken == null || characterToken.getValue().charAt(0) != ']') {
			log(LogLevel.ERROR, "missing ']' at " + tokenStream);
			return ParseOutcomes.createParseError(parsedToPosition, "Expected closing bracket ']'", ErrorPriority.RIGHT_PARSER);
		}

		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available at " + tokenStream);
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		// propagate parse outcome with corrected position (includes ']')
		return isCompile()
				? compileArraySize(sizeParseResult, tokenStream.getPosition())
				: ParseOutcomes.createObjectParseResult(tokenStream.getPosition(), sizeParseResult.getObjectInfo());
	}

	private List<ParseOutcome> parseArrayElements(TokenStream tokenStream, ParseExpectation expectation) {
		List<ParseOutcome> elements = new ArrayList<>();

		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '{') {
			log(LogLevel.ERROR, "missing '{'");
			elements.add(ParseOutcomes.createParseError(position, "Expected opening curly bracket '{'", ErrorPriority.RIGHT_PARSER));
			return elements;
		}

		if (!characterToken.isContainsCaret()) {
			if (!tokenStream.hasMore()) {
				elements.add(ParseOutcomes.createParseError(tokenStream.getPosition(), "Expected element or closing curly bracket '}'", ErrorPriority.RIGHT_PARSER));
				return elements;
			}

			char nextCharacter = tokenStream.peekCharacter();
			if (nextCharacter == '}') {
				tokenStream.readCharacterUnchecked();
				return elements;
			}
		}

		while (true) {
			/*
			 * Parse expression for argument i
			 */
			ParseOutcome element = parserToolbox.getExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), expectation);

			Optional<ParseOutcome> elementForPropagation = ParseUtils.prepareParseOutcomeForPropagation(element, expectation, ErrorPriority.RIGHT_PARSER);
			if (elementForPropagation.isPresent()) {
				elements.add(elementForPropagation.get());
				return elements;
			}
			elements.add(element);

			ObjectParseResult parseResult = ((ObjectParseResult) element);
			int parsedToPosition = parseResult.getPosition();
			tokenStream.moveTo(parsedToPosition);

			position = tokenStream.getPosition();
			characterToken = tokenStream.readCharacterUnchecked();

			if (characterToken == null) {
				elements.add(ParseOutcomes.createParseError(position, "Expected comma ',' or closing curly bracket '}'", ErrorPriority.RIGHT_PARSER));
				return elements;
			}

			if (characterToken.getValue().charAt(0) == '}') {
				if (characterToken.isContainsCaret()) {
					// nothing we can suggest after '}'
					elements.add(CompletionSuggestions.none(tokenStream.getPosition()));
				}
				return elements;
			}

			if (characterToken.getValue().charAt(0) != ',') {
				elements.add(ParseOutcomes.createParseError(position, "Expected comma ',' or closing curly bracket '}'", ErrorPriority.RIGHT_PARSER));
				return elements;
			}
		}
	}

	private List<ExecutableInfo> getConstructorInfos(TypeInfo constructorType) {
		AccessModifier minimumAccessLevel = parserToolbox.getSettings().getMinimumAccessLevel();
		ConstructorScanner constructorScanner = new ConstructorScanner().minimumAccessLevel(minimumAccessLevel);
		return InfoProvider.getConstructorInfos(constructorType, constructorScanner);
	}

	private static String formatConstructorInfo(ExecutableInfo constructorInfo) {
		return constructorInfo.getName()
				+ "("
				+ constructorInfo.formatArguments()
				+ ")";
	}

	private ParseOutcome compileObjectConstructorParseResult(ExecutableInfo constructorInfo, List<ParseOutcome> argumentParseOutcomes, int position, ObjectInfo constructorReturnInfo) {
		List<CompiledObjectParseResult> compiledArgumentParseResults = (List) argumentParseOutcomes;
		return new CompileObjectConstructorParseResult(constructorInfo, compiledArgumentParseResults, position, constructorReturnInfo);
	}

	private ParseOutcome compileArrayConstructorWithInitializerListParseResult(TypeInfo componentType, List<ParseOutcome> elementParseOutcomes, int position, ObjectInfo arrayInfo) {
		List<CompiledObjectParseResult> compiledElementParseResults = (List) elementParseOutcomes;
		return new CompiledArrayConstructorWithInitializerListParseResult(componentType, compiledElementParseResults, position, arrayInfo);
	}

	private ParseOutcome compileArrayConstructorWithDefaultInitialization(TypeInfo componentType, ParseResult sizeParseResult, int position, ObjectInfo arrayInfo) {
		CompiledObjectParseResult compiledSizeParseResult = (CompiledObjectParseResult) sizeParseResult;
		return new CompiledArrayConstructorWithDefaultInitializationParseResult(componentType, compiledSizeParseResult, position, arrayInfo);
	}

	private ParseOutcome compileArraySize(ObjectParseResult sizeParseResult, int position) {
		if (!ParseOutcomes.isCompiledParseResult(sizeParseResult)) {
			return sizeParseResult;
		}
		CompiledObjectParseResult compiledSizeParseResult = (CompiledObjectParseResult) sizeParseResult;
		return new CompiledArraySizeParseResult(compiledSizeParseResult, position);
	}

	private static class CompileObjectConstructorParseResult extends AbstractCompiledParseResult
	{
		private final ExecutableInfo					constructorInfo;
		private final List<CompiledObjectParseResult>	compiledArgumentParseResults;

		CompileObjectConstructorParseResult(ExecutableInfo constructorInfo, List<CompiledObjectParseResult> compiledArgumentParseResults, int position, ObjectInfo constructorReturnInfo) {
			super(position, constructorReturnInfo);
			this.constructorInfo = constructorInfo;
			this.compiledArgumentParseResults = compiledArgumentParseResults;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			List<ObjectInfo> argumentInfos = new ArrayList<>(compiledArgumentParseResults.size());
			for (CompiledObjectParseResult compiledArgumentParseResult : compiledArgumentParseResults) {
				argumentInfos.add(compiledArgumentParseResult.evaluate(thisInfo, thisInfo));
			}
			return OBJECT_INFO_PROVIDER.getExecutableReturnInfo(null, constructorInfo, argumentInfos);
		}
	}

	private static class CompiledArrayConstructorWithInitializerListParseResult extends AbstractCompiledParseResult
	{
		private final TypeInfo							componentType;
		private final List<CompiledObjectParseResult>	compiledElementParseResults;

		CompiledArrayConstructorWithInitializerListParseResult(TypeInfo componentType, List<CompiledObjectParseResult> compiledElementParseResults, int position, ObjectInfo arrayInfo) {
			super(position, arrayInfo);
			this.componentType = componentType;
			this.compiledElementParseResults = compiledElementParseResults;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			List<ObjectInfo> elementInfos = new ArrayList<>(compiledElementParseResults.size());
			for (CompiledObjectParseResult elementParseResult : compiledElementParseResults) {
				elementInfos.add(elementParseResult.evaluate(thisInfo, contextInfo));
			}
			return OBJECT_INFO_PROVIDER.getArrayInfo(componentType, elementInfos);
		}
	}

	private static class CompiledArrayConstructorWithDefaultInitializationParseResult extends AbstractCompiledParseResult
	{
		private final TypeInfo					componentType;
		private final CompiledObjectParseResult	compiledSizeParseResult;

		CompiledArrayConstructorWithDefaultInitializationParseResult(TypeInfo componentType, CompiledObjectParseResult compiledSizeParseResult, int position, ObjectInfo arrayInfo) {
			super(position, arrayInfo);
			this.componentType = componentType;
			this.compiledSizeParseResult = compiledSizeParseResult;
		}


		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			ObjectInfo sizeInfo = compiledSizeParseResult.evaluate(thisInfo, contextInfo);
			return OBJECT_INFO_PROVIDER.getArrayInfo(componentType, sizeInfo);
		}
	}

	private static class CompiledArraySizeParseResult extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult	compiledSizeParseResult;

		CompiledArraySizeParseResult(CompiledObjectParseResult compiledSizeParseResult, int position) {
			super(position, compiledSizeParseResult.getObjectInfo());
			this.compiledSizeParseResult = compiledSizeParseResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			return compiledSizeParseResult.evaluate(thisInfo, contextInfo);
		}
	}
}
