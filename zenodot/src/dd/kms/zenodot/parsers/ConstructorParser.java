package dd.kms.zenodot.parsers;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.common.ConstructorScanner;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataproviders.ExecutableDataProvider;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
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
	private static final String	NEW_KEYWORD		= "new";
	private static final String	ERROR_MESSAGE	= "Expected keyword '" + NEW_KEYWORD + "'";

	public ConstructorParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalErrorException, InternalEvaluationException {
		String keyword = tokenStream.readKeyword(TokenStream.NO_COMPLETIONS, ERROR_MESSAGE);
		if (!NEW_KEYWORD.equals(keyword)) {
			throw new InternalParseException(ERROR_MESSAGE);
		}
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		log(LogLevel.INFO, "parsing class at " + tokenStream);
		ClassParseResult classParseResult = ParseUtils.parseClass(tokenStream, parserToolbox);
		TypeInfo type = classParseResult.getType();

		char nextChar = tokenStream.readCharacter('(', '[');
		switch (nextChar) {
			case '(':
				return parseObjectConstructor(tokenStream, type);
			case '[':
				return parseArrayConstructor(tokenStream, type);
			default:
				throw new InternalErrorException(tokenStream.toString() +  ": Expected '(' or '[', but found '" + nextChar + "'. This case should have been handled earlier.");
		}
	}

	private ObjectParseResult parseObjectConstructor(TokenStream tokenStream, TypeInfo constructorType) throws InternalParseException, AmbiguousParseResultException, InternalErrorException, CodeCompletionException, InternalEvaluationException {
		Class<?> constructorClass = constructorType.getRawType();
		if (constructorClass.getEnclosingClass() != null && !Modifier.isStatic(constructorClass.getModifiers())) {
			throw new InternalParseException("Cannot instantiate non-static inner class '" + constructorClass.getName() + "'");
		}
		List<ExecutableInfo> constructorInfos = getConstructorInfos(constructorType);

		log(LogLevel.INFO, "parsing constructor arguments");
		ExecutableDataProvider executableDataProvider = parserToolbox.getExecutableDataProvider();
		List<ObjectParseResult> argumentResults = executableDataProvider.parseArguments(tokenStream, constructorInfos);
		List<ObjectInfo> argumentInfos = argumentResults.stream().map(ObjectParseResult::getObjectInfo).collect(Collectors.toList());
		List<ExecutableInfo> bestMatchingConstructorInfos = parserToolbox.getExecutableDataProvider().getBestMatchingExecutables(constructorInfos, argumentInfos);

		switch (bestMatchingConstructorInfos.size()) {
			case 0:
				throw new InternalParseException("No constructor matches the given arguments");
			case 1: {
				ExecutableInfo bestMatchingConstructorInfo = bestMatchingConstructorInfos.get(0);
				ObjectInfo constructorReturnInfo;
				try {
					log(LogLevel.SUCCESS, "found unique matching constructor");
					constructorReturnInfo = parserToolbox.getObjectInfoProvider().getExecutableReturnInfo(null, bestMatchingConstructorInfo, argumentInfos);
				} catch (Exception e) {
					throw new InternalEvaluationException("Error when trying to invoke constructor of '" + constructorClass.getSimpleName() + "': " + e.getMessage(), e);
				}
				return isCompile()
						? new CompileObjectConstructorParseResult(bestMatchingConstructorInfo, (List) argumentResults, constructorReturnInfo)
						: ParseResults.createObjectParseResult(constructorReturnInfo);
			}
			default: {
				String error = "Ambiguous constructor call. Possible candidates are:\n"
								+ bestMatchingConstructorInfos.stream().map(ConstructorParser::formatConstructorInfo).collect(Collectors.joining("\n"));
				throw new AmbiguousParseResultException(error);
			}
		}
	}

	private ObjectParseResult parseArrayConstructor(TokenStream tokenStream, TypeInfo componentType) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalErrorException, InternalEvaluationException {
		// TODO: currently, only 1d arrays are supported
		@Nullable ObjectParseResult arraySizeParseResult = parseArraySize(tokenStream);
		if (arraySizeParseResult == null) {
			// array constructor with initializer list (e.g., "new int[] { 1, 2, 3 }")
			log(LogLevel.INFO, "parsing array elements at " + tokenStream);
			ObjectParseResultExpectation elementExpectation = new ObjectParseResultExpectation(ImmutableList.of(componentType), true);
			List<ObjectParseResult> elementParseResults = parseArrayElements(tokenStream, elementExpectation);
			List<ObjectInfo> elementInfos = elementParseResults.stream().map(ObjectParseResult::getObjectInfo).collect(Collectors.toList());
			ObjectInfo arrayInfo = parserToolbox.getObjectInfoProvider().getArrayInfo(componentType, elementInfos);
			log(LogLevel.SUCCESS, "detected valid array construction with initializer list");
			return isCompile()
					? new CompiledArrayConstructorWithInitializerListParseResult(componentType, (List) elementParseResults, arrayInfo)
					: ParseResults.createObjectParseResult(arrayInfo);
		} else {
			// array constructor with default initialization (e.g., "new int[3]")
			ObjectInfo arrayInfo;
			try {
				ObjectInfo sizeInfo = arraySizeParseResult.getObjectInfo();
				arrayInfo = parserToolbox.getObjectInfoProvider().getArrayInfo(componentType, sizeInfo);
				log(LogLevel.SUCCESS, "detected valid array construction with null initialization");
			} catch (ClassCastException | NegativeArraySizeException e) {
				log(LogLevel.ERROR, "caught exception: " + e.getMessage());
				throw new InternalEvaluationException("Exception during array construction: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			}
			return isCompile()
					? new CompiledArrayConstructorWithDefaultInitializationParseResult(componentType, (CompiledObjectParseResult) arraySizeParseResult, arrayInfo)
					: ParseResults.createObjectParseResult(arrayInfo);
		}
	}

	// returns null if no size is specified (e.g. in "new int[] { 1, 2, 3 }")
	private @Nullable ObjectParseResult parseArraySize(TokenStream tokenStream) throws AmbiguousParseResultException, CodeCompletionException, InternalParseException, InternalErrorException, InternalEvaluationException {
		log(LogLevel.INFO, "parsing array size");

		if (tokenStream.skipCharacter(']')) {
			return null;
		}

		ObjectParseResultExpectation sizeExpectation = new ObjectParseResultExpectation(ImmutableList.of(InfoProvider.createTypeInfo(int.class)), true);
		ObjectParseResult arraySizeParseResult = parserToolbox.createExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), sizeExpectation);

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		tokenStream.readCharacter(']');

		return isCompile()
				? new CompiledArraySizeParseResult((CompiledObjectParseResult) arraySizeParseResult)
				: ParseResults.createObjectParseResult(arraySizeParseResult.getObjectInfo());
	}

	private List<ObjectParseResult> parseArrayElements(TokenStream tokenStream, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalErrorException, InternalEvaluationException {
		List<ObjectParseResult> elements = new ArrayList<>();

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		tokenStream.readCharacter('{');

		boolean foundClosingCurlyBrace = false;
		if (tokenStream.skipCharacter('}')) {
			foundClosingCurlyBrace = true;
		}
		while (!foundClosingCurlyBrace) {
			/*
			 * Parse expression for argument i
			 */
			ObjectParseResult element = parserToolbox.createExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), expectation);
			elements.add(element);

			foundClosingCurlyBrace = tokenStream.readCharacter(',', '}') == '}';
		}
		return elements;
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

	private static class CompileObjectConstructorParseResult extends AbstractCompiledParseResult
	{
		private final ExecutableInfo					constructor;
		private final List<CompiledObjectParseResult>	compiledArguments;

		CompileObjectConstructorParseResult(ExecutableInfo constructor, List<CompiledObjectParseResult> compiledArguments, ObjectInfo constructorReturnInfo) {
			super(constructorReturnInfo);
			this.constructor = constructor;
			this.compiledArguments = compiledArguments;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			List<ObjectInfo> arguments = new ArrayList<>(compiledArguments.size());
			for (CompiledObjectParseResult compiledArgument : compiledArguments) {
				arguments.add(compiledArgument.evaluate(thisInfo, thisInfo));
			}
			return OBJECT_INFO_PROVIDER.getExecutableReturnInfo(null, constructor, arguments);
		}
	}

	private static class CompiledArrayConstructorWithInitializerListParseResult extends AbstractCompiledParseResult
	{
		private final TypeInfo							componentType;
		private final List<CompiledObjectParseResult>	compiledElementParseResults;

		CompiledArrayConstructorWithInitializerListParseResult(TypeInfo componentType, List<CompiledObjectParseResult> compiledElementParseResults, ObjectInfo arrayInfo) {
			super(arrayInfo);
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

		CompiledArrayConstructorWithDefaultInitializationParseResult(TypeInfo componentType, CompiledObjectParseResult compiledSizeParseResult, ObjectInfo arrayInfo) {
			super(arrayInfo);
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

		CompiledArraySizeParseResult(CompiledObjectParseResult compiledSizeParseResult) {
			super(compiledSizeParseResult.getObjectInfo());
			this.compiledSizeParseResult = compiledSizeParseResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			return compiledSizeParseResult.evaluate(thisInfo, contextInfo);
		}
	}
}
