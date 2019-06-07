package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.matching.*;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionKeyword;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import static dd.kms.zenodot.result.ParseError.ErrorPriority;

/**
 * Parses literals in the context of {@code this}. Supported types of literals are:
 * <ul>
 *     <li>true</li>
 *     <li>false</li>
 *     <li>null</li>
 *     <li>this</li>
 *     <li>string literals</li>
 *     <li>character literals</li>
 *     <li>int literals</li>
 *     <li>long literals (with the suffix {@code L})</li>
 *     <li>float literals (with the suffix {@code f})</li>
 *     <li>double literals</li>
 * </ul>
 */
public class LiteralParser extends AbstractEntityParser<ObjectInfo>
{
	private static final ObjectInfo	TRUE_INFO	= InfoProvider.createObjectInfo(true, InfoProvider.createTypeInfo(boolean.class));
	private static final ObjectInfo	FALSE_INFO	= InfoProvider.createObjectInfo(false, InfoProvider.createTypeInfo(boolean.class));
	private static final ObjectInfo	NULL_INFO	= InfoProvider.createObjectInfo(null, InfoProvider.NO_TYPE);

	private final AbstractEntityParser<ObjectInfo> intParser;
	private final AbstractEntityParser<ObjectInfo> longParser;
	private final AbstractEntityParser<ObjectInfo> floatParser;
	private final AbstractEntityParser<ObjectInfo> doubleParser;

	public LiteralParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
		intParser 		= new NumericLiteralParser<>(parserToolbox, thisInfo, InfoProvider.createTypeInfo(int.class),		TokenStream::readIntegerLiteral,	Integer::parseInt,		"Invalid int literal");
		longParser 		= new NumericLiteralParser<>(parserToolbox, thisInfo, InfoProvider.createTypeInfo(long.class),		TokenStream::readLongLiteral, 		Long::parseLong,		"Invalid long literal");
		floatParser 	= new NumericLiteralParser<>(parserToolbox, thisInfo, InfoProvider.createTypeInfo(float.class),		TokenStream::readFloatLiteral,		Float::parseFloat,		"Invalid float literal");
		doubleParser 	= new NumericLiteralParser<>(parserToolbox, thisInfo, InfoProvider.createTypeInfo(double.class),	TokenStream::readDoubleLiteral,		Double::parseDouble,	"Invalid double literal");
	}

	@Override
	ParseOutcome doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		if (!tokenStream.hasMore()) {
			return ParseOutcomes.createParseError(tokenStream.getPosition(), "Expected a literal", ErrorPriority.WRONG_PARSER);
		}
		String characters = tokenStream.peekCharacters();
		if (characters.startsWith("\"")) {
			return parseStringLiteral(tokenStream, expectation);
		} else if (characters.startsWith("'")) {
			return parseCharacterLiteral(tokenStream, expectation);
		} else if (characters.startsWith("tr")) {
			return parseNamedLiteral(tokenStream, "true", TRUE_INFO, expectation);
		} else if (characters.startsWith("f")) {
			return parseNamedLiteral(tokenStream, "false", FALSE_INFO, expectation);
		} else if (characters.startsWith("n")) {
			return parseNamedLiteral(tokenStream, "null", NULL_INFO, expectation);
		} else if (characters.startsWith("th")) {
			return parseNamedLiteral(tokenStream, "this", thisInfo, expectation);
		} else {
			return parseNumericLiteral(tokenStream, contextInfo, expectation);
		}
	}

	private ParseOutcome parseStringLiteral(TokenStream tokenStream, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		Token stringLiteralToken;
		try {
			stringLiteralToken = tokenStream.readStringLiteral();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "expected a string literal at " + tokenStream);
			return ParseOutcomes.createParseError(startPosition, "Expected a string literal", ErrorPriority.RIGHT_PARSER);
		}
		if (stringLiteralToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available for string literals");
			return CompletionSuggestions.none(tokenStream.getPosition());
		}
		String stringLiteralValue = stringLiteralToken.getValue();
		log(LogLevel.SUCCESS, "detected string literal '" + stringLiteralValue + "'");

		ObjectInfo stringLiteralInfo = InfoProvider.createObjectInfo(stringLiteralValue, InfoProvider.createTypeInfo(String.class));
		return parserToolbox.getObjectTailParser().parse(tokenStream, stringLiteralInfo, expectation);
	}

	private ParseOutcome parseCharacterLiteral(TokenStream tokenStream, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		Token characterLiteralToken;
		try {
			characterLiteralToken = tokenStream.readCharacterLiteral();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "expected a character literal at " + tokenStream);
			return ParseOutcomes.createParseError(startPosition, "Expected a character literal", ErrorPriority.RIGHT_PARSER);
		}
		if (characterLiteralToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available for character literals");
			return CompletionSuggestions.none(tokenStream.getPosition());
		}
		String characterLiteralValue = characterLiteralToken.getValue();
		if (characterLiteralValue.length() != 1) {
			throw new IllegalStateException("Internal error parsing character literals. It should represent exactly 1 character, but it represents " + characterLiteralValue.length());
		}
		log(LogLevel.SUCCESS, "detected character literal '" + characterLiteralValue + "'");

		ObjectInfo stringLiteralInfo = InfoProvider.createObjectInfo(characterLiteralValue.charAt(0), InfoProvider.createTypeInfo(char.class));
		return parserToolbox.getObjectTailParser().parse(tokenStream, stringLiteralInfo, expectation);
	}

	private ParseOutcome parseNamedLiteral(TokenStream tokenStream, String literalName, ObjectInfo literalInfo, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		Token literalToken = tokenStream.readKeyWordUnchecked();
		if (literalToken == null) {
			log(LogLevel.ERROR, "expected literal '" + literalName + "'");
			return ParseOutcomes.createParseError(startPosition, "Expected '" + literalName + "'", ErrorPriority.WRONG_PARSER);
		}
		if (literalToken.isContainsCaret()) {
			if (literalName.startsWith(literalToken.getValue())) {
				MatchRating rating = MatchRatings.create(StringMatch.PREFIX, TypeMatch.NONE, AccessMatch.IGNORED);
				CompletionSuggestion suggestion = new CompletionSuggestionKeyword(literalName, startPosition, tokenStream.getPosition());
				log(LogLevel.INFO, "suggesting literal '" + literalName + "'...");
				return CompletionSuggestions.of(suggestion, rating);

			} else {
				log(LogLevel.INFO, "no completion suggestions available");
				return CompletionSuggestions.none(tokenStream.getPosition());
			}
		}
		if (!literalToken.getValue().equals(literalName)) {
			log(LogLevel.ERROR, "expected literal '" + literalName + "'");
			return ParseOutcomes.createParseError(startPosition, "Expected '" + literalName + "'", ErrorPriority.WRONG_PARSER);
		}
		log(LogLevel.SUCCESS, "detected literal '" + literalName + "'");
		return parserToolbox.getObjectTailParser().parse(tokenStream, literalInfo, expectation);
	}

	private ParseOutcome parseNumericLiteral(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();
		char c = tokenStream.peekCharacter();
		if (!"+-.0123456789".contains(String.valueOf(c))) {
			log(LogLevel.ERROR, "expected a numeric literal");
			return ParseOutcomes.createParseError(startPosition, "Expected a literal", ErrorPriority.WRONG_PARSER);
		}

		AbstractEntityParser[] parsers = { longParser, intParser, floatParser, doubleParser };
		for (AbstractEntityParser parser : parsers) {
			ParseOutcome parseOutcome = parser.parse(tokenStream, contextInfo, expectation);

			if (parseOutcome.getOutcomeType() != ParseOutcomeType.PARSE_ERROR) {
				return parseOutcome;
			}
		}
		log(LogLevel.ERROR, "expected a numeric literal");
		return ParseOutcomes.createParseError(startPosition, "Expected a numeric literal", ErrorPriority.WRONG_PARSER);
	}

	private static class NumericLiteralParser<T> extends AbstractEntityParser<ObjectInfo>
	{
		private final TypeInfo				numericType;
		private final NumericTokenReader	tokenReader;
		private final NumericValueParser<T>	valueParser;
		private final String				wrongTypeError;

		NumericLiteralParser(ParserToolbox parserToolbox, ObjectInfo thisInfo, TypeInfo numericType, NumericTokenReader tokenReader, NumericValueParser<T> valueParser, String wrongTypeError) {
			super(parserToolbox, thisInfo);
			this.numericType = numericType;
			this.tokenReader = tokenReader;
			this.valueParser = valueParser;
			this.wrongTypeError = wrongTypeError;
		}

		@Override
		ParseOutcome doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
			int startPosition = tokenStream.getPosition();
			Token token;
			try {
				token = tokenReader.read(tokenStream);
			} catch (TokenStream.JavaTokenParseException e) {
				return ParseOutcomes.createParseError(startPosition, wrongTypeError, ErrorPriority.WRONG_PARSER);
			}
			if (token.isContainsCaret()) {
				log(LogLevel.INFO, "no completion suggestions available");
				return CompletionSuggestions.none(tokenStream.getPosition());
			}

			T literalValue;
			try {
				literalValue = valueParser.parse(token.getValue());
				log(LogLevel.SUCCESS, "detected numeric literal '" + token.getValue() + "'");
			} catch (NumberFormatException e) {
				log(LogLevel.ERROR, "number format exception: " + e.getMessage());
				return ParseOutcomes.createParseError(startPosition, wrongTypeError, ErrorPriority.WRONG_PARSER);
			}

			ObjectInfo literalInfo = InfoProvider.createObjectInfo(literalValue, numericType);
			return parserToolbox.getObjectTailParser().parse(tokenStream, literalInfo, expectation);
		}
	}

	@FunctionalInterface
	private interface NumericTokenReader
	{
		Token read(TokenStream tokenStream) throws TokenStream.JavaTokenParseException;
	}

	@FunctionalInterface
	private interface NumericValueParser<T>
	{
		T parse(String s) throws NumberFormatException;
	}
}
