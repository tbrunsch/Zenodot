package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorType;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataProviders.ExecutableDataProvider;
import dd.kms.zenodot.utils.wrappers.AbstractExecutableInfo;
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
public class ConstructorParser extends AbstractEntityParser<ObjectInfo>
{
	public ConstructorParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		Token operatorToken = tokenStream.readKeyWordUnchecked();
		if (operatorToken == null) {
			log(LogLevel.ERROR, "'new' operator expected");
			return new ParseError(startPosition, "Expected operator 'new'", ErrorType.WRONG_PARSER);
		}
		if (operatorToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available");
			return CompletionSuggestions.none(tokenStream.getPosition());
		}
		if (!operatorToken.getValue().equals("new")) {
			log(LogLevel.ERROR, "'new' operator expected");
			return new ParseError(startPosition, "Expected operator 'new'", ErrorType.WRONG_PARSER);
		}

		log(LogLevel.INFO, "parsing class at " + tokenStream);
		ParseResult classParseResult = parserToolbox.getClassParser().parse(tokenStream, thisInfo, ParseExpectation.CLASS);
		ParseResultType parseResultType = classParseResult.getResultType();
		log(LogLevel.INFO, "parse result: " + parseResultType);

		if (ParseUtils.propagateParseResult(classParseResult, ParseExpectation.CLASS)) {
			return classParseResult;
		}
		ClassParseResult parseResult = (ClassParseResult) classParseResult;
		int parsedToPosition = parseResult.getPosition();
		TypeInfo type = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		char nextChar = tokenStream.peekCharacter();
		if (nextChar == '(') {
			return parseObjectConstructor(tokenStream, startPosition, type, expectation);
		} else if (nextChar == '[') {
			return parseArrayConstructor(tokenStream, startPosition, type, expectation);
		} else {
			log(LogLevel.ERROR, "missing '(' at " + tokenStream);
			return new ParseError(tokenStream.getPosition(), "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}
	}

	private ParseResult parseObjectConstructor(TokenStream tokenStream, int startPosition, TypeInfo constructorType, ParseExpectation expectation) {
		Class<?> constructorClass = constructorType.getRawType();
		if (constructorClass.getEnclosingClass() != null && !Modifier.isStatic(constructorClass.getModifiers())) {
			log(LogLevel.ERROR, "cannot instantiate non-static inner class");
			return new ParseError(tokenStream.getPosition(), "Cannot instantiate inner class '" + constructorClass.getName() + "'", ErrorType.SEMANTIC_ERROR);
		}
		List<AbstractExecutableInfo> constructorInfos = parserToolbox.getInspectionDataProvider().getConstructorInfos(constructorType);

		log(LogLevel.INFO, "parsing constructor arguments");
		ExecutableDataProvider executableDataProvider = parserToolbox.getExecutableDataProvider();
		List<ParseResult> argumentParseResults = executableDataProvider.parseExecutableArguments(tokenStream, constructorInfos);

		if (argumentParseResults.isEmpty()) {
			log(LogLevel.INFO, "no arguments found");
		} else {
			int lastArgumentIndex = argumentParseResults.size() - 1;
			ParseResult lastArgumentParseResult = argumentParseResults.get(lastArgumentIndex);
			ParseResultType lastArgumentParseResultType = lastArgumentParseResult.getResultType();
			log(LogLevel.INFO, "parse result: " + lastArgumentParseResultType);

			if (lastArgumentParseResult.getResultType() == ParseResultType.COMPLETION_SUGGESTIONS) {
				CompletionSuggestions argumentSuggestions = (CompletionSuggestions) lastArgumentParseResult;
				// add argument information
				if (argumentSuggestions.getExecutableArgumentInfo().isPresent()) {
					// information has already been added for an executable used in a subexpression, which is more relevant
					return argumentSuggestions;
				}
				List<ObjectInfo> previousArgumentInfos = argumentParseResults.subList(0, lastArgumentIndex).stream()
					.map(ObjectParseResult.class::cast)
					.map(ObjectParseResult::getObjectInfo)
					.collect(Collectors.toList());
				ExecutableArgumentInfo executableArgumentInfo = executableDataProvider.createExecutableArgumentInfo(constructorInfos, previousArgumentInfos);
				return new CompletionSuggestions(argumentSuggestions.getPosition(), argumentSuggestions.getRatedSuggestions(), Optional.of(executableArgumentInfo));
			}

			if (ParseUtils.propagateParseResult(lastArgumentParseResult, ParseExpectation.OBJECT)) {
				return lastArgumentParseResult;
			}
		}

		List<ObjectInfo> argumentInfos = argumentParseResults.stream()
			.map(ObjectParseResult.class::cast)
			.map(ObjectParseResult::getObjectInfo)
			.collect(Collectors.toList());
		List<AbstractExecutableInfo> bestMatchingConstructorInfos = parserToolbox.getExecutableDataProvider().getBestMatchingExecutableInfos(constructorInfos, argumentInfos);

		switch (bestMatchingConstructorInfos.size()) {
			case 0:
				log(LogLevel.ERROR, "no matching constructor found");
				return new ParseError(tokenStream.getPosition(), "No constructor matches the given arguments", ErrorType.SEMANTIC_ERROR);
			case 1: {
				AbstractExecutableInfo bestMatchingConstructorInfo = bestMatchingConstructorInfos.get(0);
				ObjectInfo constructorReturnInfo;
				try {
					constructorReturnInfo = parserToolbox.getObjectInfoProvider().getExecutableReturnInfo(null, bestMatchingConstructorInfo, argumentInfos);
					log(LogLevel.SUCCESS, "found unique matching constructor");
				} catch (Exception e) {
					log(LogLevel.ERROR, "caught exception: " + e.getMessage());
					return new ParseError(startPosition, "Exception during constructor evaluation", ErrorType.EVALUATION_EXCEPTION, e);
				}
				return parserToolbox.getObjectTailParser().parse(tokenStream, constructorReturnInfo, expectation);
			}
			default: {
				String error = "Ambiguous constructor call. Possible candidates are:\n"
								+ bestMatchingConstructorInfos.stream().map(ConstructorParser::formatConstructorInfo).collect(Collectors.joining("\n"));
				log(LogLevel.ERROR, error);
				return new AmbiguousParseResult(tokenStream.getPosition(), error);
			}
		}
	}

	private ParseResult parseArrayConstructor(TokenStream tokenStream, int startPosition, TypeInfo componentType, ParseExpectation expectation) {
		// TODO: currently, only 1d arrays are supported
		ParseResult arraySizeParseResult = parseArraySize(tokenStream);
		if (arraySizeParseResult == null) {
			// array constructor with initializer list (e.g., "new int[] { 1, 2, 3 }")
			log(LogLevel.INFO, "parsing array elements at " + tokenStream);
			List<ParseResult> elementParseResults = parseArrayElements(tokenStream, ParseExpectationBuilder.expectObject().allowedType(componentType).build());

			if (elementParseResults.isEmpty()) {
				log(LogLevel.INFO, "detected empty array");
			} else {
				ParseResult lastArgumentParseResult = elementParseResults.get(elementParseResults.size()-1);
				ParseResultType lastArgumentParseResultType = lastArgumentParseResult.getResultType();
				log(LogLevel.INFO, "parse result: " + lastArgumentParseResultType);

				if (ParseUtils.propagateParseResult(lastArgumentParseResult, ParseExpectation.OBJECT)) {
					return lastArgumentParseResult;
				}
			}

			List<ObjectInfo> elementInfos = elementParseResults.stream().map(ObjectParseResult.class::cast).map(ObjectParseResult::getObjectInfo).collect(Collectors.toList());
			ObjectInfo arrayInfo = parserToolbox.getObjectInfoProvider().getArrayInfo(componentType, elementInfos);
			log (LogLevel.SUCCESS, "detected valid array construction with initializer list");
			return parserToolbox.getObjectTailParser().parse(tokenStream, arrayInfo, expectation);
		} else {
			// array constructor with default initialization (e.g., "new int[3]")
			if (ParseUtils.propagateParseResult(arraySizeParseResult, ParseExpectation.OBJECT)) {
				return arraySizeParseResult;
			}
			ObjectParseResult parseResult = (ObjectParseResult) arraySizeParseResult;
			int parsedToPosition = parseResult.getPosition();
			ObjectInfo sizeInfo = parseResult.getObjectInfo();
			ObjectInfo arrayInfo;
			try {
				arrayInfo = parserToolbox.getObjectInfoProvider().getArrayInfo(componentType, sizeInfo);
				log(LogLevel.SUCCESS, "detected valid array construction with null initialization");
			} catch (ClassCastException | NegativeArraySizeException e) {
				log(LogLevel.ERROR, "caught exception: " + e.getMessage());
				return new ParseError(startPosition, e.getClass().getSimpleName() + " during array construction", ErrorType.EVALUATION_EXCEPTION, e);
			}
			tokenStream.moveTo(parsedToPosition);
			return parserToolbox.getObjectTailParser().parse(tokenStream, arrayInfo, expectation);
		}
	}

	// returns null if no size is specified (e.g. in "new int[] { 1, 2, 3 }")
	private ParseResult parseArraySize(TokenStream tokenStream) {
		log(LogLevel.INFO, "parsing array size");

		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals("[");

		if (tokenStream.peekCharacter() == ']') {
			tokenStream.readCharacterUnchecked();
			return null;
		}

		ParseExpectation expectation = ParseExpectationBuilder.expectObject().allowedType(TypeInfo.of(int.class)).build();
		ParseResult arraySizeParseResult = parserToolbox.getExpressionParser().parse(tokenStream, thisInfo, expectation);

		if (ParseUtils.propagateParseResult(arraySizeParseResult, expectation)) {
			return arraySizeParseResult;
		}

		ObjectParseResult parseResult = ((ObjectParseResult) arraySizeParseResult);
		int parsedToPosition = parseResult.getPosition();

		tokenStream.moveTo(parsedToPosition);
		characterToken = tokenStream.readCharacterUnchecked();

		if (characterToken == null || characterToken.getValue().charAt(0) != ']') {
			log(LogLevel.ERROR, "missing ']' at " + tokenStream);
			return new ParseError(parsedToPosition, "Expected closing bracket ']'", ErrorType.SYNTAX_ERROR);
		}

		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available at " + tokenStream);
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		// propagate parse result with corrected position (includes ']')
		return new ObjectParseResult(tokenStream.getPosition(), parseResult.getObjectInfo());
	}

	private List<ParseResult> parseArrayElements(TokenStream tokenStream, ParseExpectation expectation) {
		List<ParseResult> elements = new ArrayList<>();

		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '{') {
			log(LogLevel.ERROR, "missing '{'");
			elements.add(new ParseError(position, "Expected opening curly bracket '{'", ErrorType.SYNTAX_ERROR));
			return elements;
		}

		if (!characterToken.isContainsCaret()) {
			if (!tokenStream.hasMore()) {
				elements.add(new ParseError(tokenStream.getPosition(), "Expected element or closing curly bracket '}'", ParseError.ErrorType.SYNTAX_ERROR));
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
			ParseResult element = parserToolbox.getExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), expectation);
			elements.add(element);

			if (ParseUtils.propagateParseResult(element, expectation)) {
				return elements;
			}

			ObjectParseResult parseResult = ((ObjectParseResult) element);
			int parsedToPosition = parseResult.getPosition();
			tokenStream.moveTo(parsedToPosition);

			position = tokenStream.getPosition();
			characterToken = tokenStream.readCharacterUnchecked();

			if (characterToken == null) {
				elements.add(new ParseError(position, "Expected comma ',' or closing curly bracket '}'", ParseError.ErrorType.SYNTAX_ERROR));
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
				elements.add(new ParseError(position, "Expected comma ',' or closing curly bracket '}'", ParseError.ErrorType.SYNTAX_ERROR));
				return elements;
			}
		}
	}

	private static String formatConstructorInfo(AbstractExecutableInfo constructorInfo) {
		return constructorInfo.getName()
				+ "("
				+ constructorInfo.formatArguments()
				+ ")";
	}
}
