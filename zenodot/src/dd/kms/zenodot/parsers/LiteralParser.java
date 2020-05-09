package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.matching.TypeMatch;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.codecompletions.CodeCompletionFactory;
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
public class LiteralParser extends AbstractParserWithObjectTail<ObjectInfo>
{
	private static final ObjectInfo	TRUE_INFO	= InfoProvider.createObjectInfo(true, InfoProvider.createTypeInfo(boolean.class));
	private static final ObjectInfo	FALSE_INFO	= InfoProvider.createObjectInfo(false, InfoProvider.createTypeInfo(boolean.class));

	private final AbstractParser<ObjectInfo> intParser;
	private final AbstractParser<ObjectInfo> longParser;
	private final AbstractParser<ObjectInfo> floatParser;
	private final AbstractParser<ObjectInfo> doubleParser;

	public LiteralParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
		intParser 		= new NumericLiteralParser<>(parserToolbox, InfoProvider.createTypeInfo(int.class),		TokenStream::readIntegerLiteral,	Integer::parseInt,		"Invalid int literal");
		longParser 		= new NumericLiteralParser<>(parserToolbox, InfoProvider.createTypeInfo(long.class),	TokenStream::readLongLiteral, 		Long::parseLong,		"Invalid long literal");
		floatParser 	= new NumericLiteralParser<>(parserToolbox, InfoProvider.createTypeInfo(float.class),	TokenStream::readFloatLiteral,		Float::parseFloat,		"Invalid float literal");
		doubleParser 	= new NumericLiteralParser<>(parserToolbox, InfoProvider.createTypeInfo(double.class),	TokenStream::readDoubleLiteral,		Double::parseDouble,	"Invalid double literal");
	}

	@Override
	ParseOutcome parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		if (!tokenStream.hasMore()) {
			return ParseOutcomes.createParseError(tokenStream.getPosition(), "Expected a literal", ErrorPriority.WRONG_PARSER);
		}
		ParseOutcome parseOutcome = parseWithoutCompilingConstantLiterals(tokenStream, contextInfo);
		boolean needsCompilation = isCompile()
								&& ParseOutcomes.isParseResultOfType(parseOutcome, ParseResultType.OBJECT)
								&& !ParseOutcomes.isCompiledParseResult(parseOutcome);
		if (needsCompilation) {
			ObjectParseResult parseResult = (ObjectParseResult) parseOutcome;
			return ParseOutcomes.createCompiledConstantObjectParseResult(parseResult.getPosition(), parseResult.getObjectInfo());
		}
		return parseOutcome;
	}

	private ParseOutcome parseWithoutCompilingConstantLiterals(TokenStream tokenStream, ObjectInfo contextInfo) {
		String characters = tokenStream.peekCharacters();
		if (characters.startsWith("\"")) {
			return parseStringLiteral(tokenStream);
		} else if (characters.startsWith("'")) {
			return parseCharacterLiteral(tokenStream);
		} else if (characters.startsWith("tr")) {
			return parseNamedLiteral(tokenStream, "true", TRUE_INFO);
		} else if (characters.startsWith("f")) {
			return parseNamedLiteral(tokenStream, "false", FALSE_INFO);
		} else if (characters.startsWith("n")) {
			return parseNamedLiteral(tokenStream, "null", InfoProvider.NULL_LITERAL);
		} else if (characters.startsWith("th")) {
			return parseNamedLiteral(tokenStream, "this", parserToolbox.getThisInfo());
		} else {
			return parseNumericLiteral(tokenStream, contextInfo);
		}
	}

	private ParseOutcome parseStringLiteral(TokenStream tokenStream) {
		int startPosition = tokenStream.getPosition();
		Token stringLiteralToken;
		try {
			stringLiteralToken = tokenStream.readStringLiteral();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "expected a string literal at " + tokenStream);
			return ParseOutcomes.createParseError(startPosition, "Expected a string literal", ErrorPriority.RIGHT_PARSER);
		}
		if (stringLiteralToken.isContainsCaret()) {
			log(LogLevel.INFO, "no code completions available for string literals");
			return CodeCompletions.none(tokenStream.getPosition());
		}
		String stringLiteralValue = stringLiteralToken.getValue();
		log(LogLevel.SUCCESS, "detected string literal '" + stringLiteralValue + "'");

		ObjectInfo stringLiteralInfo = InfoProvider.createObjectInfo(stringLiteralValue, InfoProvider.createTypeInfo(String.class));
		return ParseOutcomes.createObjectParseResult(tokenStream.getPosition(), stringLiteralInfo);
	}

	private ParseOutcome parseCharacterLiteral(TokenStream tokenStream) {
		int startPosition = tokenStream.getPosition();
		Token characterLiteralToken;
		try {
			characterLiteralToken = tokenStream.readCharacterLiteral();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "expected a character literal at " + tokenStream);
			return ParseOutcomes.createParseError(startPosition, "Expected a character literal", ErrorPriority.RIGHT_PARSER);
		}
		if (characterLiteralToken.isContainsCaret()) {
			log(LogLevel.INFO, "no code completions available for character literals");
			return CodeCompletions.none(tokenStream.getPosition());
		}
		String characterLiteralValue = characterLiteralToken.getValue();
		if (characterLiteralValue.length() != 1) {
			throw new IllegalStateException("Internal error parsing character literals. It should represent exactly 1 character, but it represents " + characterLiteralValue.length());
		}
		log(LogLevel.SUCCESS, "detected character literal '" + characterLiteralValue + "'");

		ObjectInfo characterLiteralInfo = InfoProvider.createObjectInfo(characterLiteralValue.charAt(0), InfoProvider.createTypeInfo(char.class));
		return ParseOutcomes.createObjectParseResult(tokenStream.getPosition(), characterLiteralInfo);
	}

	private ParseOutcome parseNamedLiteral(TokenStream tokenStream, String literalName, ObjectInfo literalInfo) {
		int startPosition = tokenStream.getPosition();
		Token literalToken = tokenStream.readKeyWordUnchecked();
		if (literalToken == null) {
			log(LogLevel.ERROR, "expected literal '" + literalName + "'");
			return ParseOutcomes.createParseError(startPosition, "Expected '" + literalName + "'", ErrorPriority.WRONG_PARSER);
		}
		if (literalToken.isContainsCaret()) {
			if (literalName.startsWith(literalToken.getValue())) {
				MatchRating rating = MatchRatings.create(StringMatch.PREFIX, TypeMatch.NONE, false);
				CodeCompletion codeCompletion = CodeCompletionFactory.keywordCompletion(literalName, startPosition, tokenStream.getPosition(), rating);
				log(LogLevel.INFO, "suggesting literal '" + literalName + "'...");
				return CodeCompletions.of(codeCompletion);

			} else {
				log(LogLevel.INFO, "no code completions available");
				return CodeCompletions.none(tokenStream.getPosition());
			}
		}
		if (!literalToken.getValue().equals(literalName)) {
			log(LogLevel.ERROR, "expected literal '" + literalName + "'");
			return ParseOutcomes.createParseError(startPosition, "Expected '" + literalName + "'", ErrorPriority.WRONG_PARSER);
		}
		log(LogLevel.SUCCESS, "detected literal '" + literalName + "'");
		int position = tokenStream.getPosition();
		return isCompile() && "this".equals(literalName)
				? new CompiledThisParseResult(position, literalInfo)
				: ParseOutcomes.createObjectParseResult(position, literalInfo);
	}

	private ParseOutcome parseNumericLiteral(TokenStream tokenStream, ObjectInfo contextInfo) {
		int startPosition = tokenStream.getPosition();
		char c = tokenStream.peekCharacter();
		if (!"+-.0123456789".contains(String.valueOf(c))) {
			log(LogLevel.ERROR, "expected a numeric literal");
			return ParseOutcomes.createParseError(startPosition, "Expected a literal", ErrorPriority.WRONG_PARSER);
		}

		AbstractParser[] parsers = { longParser, intParser, floatParser, doubleParser };
		for (AbstractParser parser : parsers) {
			ParseOutcome parseOutcome = parser.parse(tokenStream, contextInfo, ParseExpectation.OBJECT);

			if (parseOutcome.getOutcomeType() != ParseOutcomeType.ERROR) {
				return parseOutcome;
			}
		}
		log(LogLevel.ERROR, "expected a numeric literal");
		return ParseOutcomes.createParseError(startPosition, "Expected a numeric literal", ErrorPriority.WRONG_PARSER);
	}

	private static class CompiledThisParseResult extends AbstractCompiledParseResult
	{
		CompiledThisParseResult(int position, ObjectInfo thisInfo) {
			super(position, thisInfo);
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			return thisInfo;
		}
	}

	private static class NumericLiteralParser<T> extends AbstractParser<ObjectInfo>
	{
		private final TypeInfo				numericType;
		private final NumericTokenReader	tokenReader;
		private final NumericValueParser<T>	valueParser;
		private final String				wrongTypeError;

		NumericLiteralParser(ParserToolbox parserToolbox, TypeInfo numericType, NumericTokenReader tokenReader, NumericValueParser<T> valueParser, String wrongTypeError) {
			super(parserToolbox);
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
				log(LogLevel.INFO, "no code completions available");
				return CodeCompletions.none(tokenStream.getPosition());
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
			return ParseOutcomes.createObjectParseResult(tokenStream.getPosition(), literalInfo);
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
