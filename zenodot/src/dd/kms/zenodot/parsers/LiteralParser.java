package dd.kms.zenodot.parsers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.matching.TypeMatch;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.codecompletions.CodeCompletionFactory;
import dd.kms.zenodot.tokenizer.CompletionInfo;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;
import java.util.Map;

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

	private static final String		NULL_LITERAL	= "null";
	private static final String		THIS_LITERAL	= "this";
	private static final String		TRUE_LITERAL	= "true";
	private static final String		FALSE_LITERAL	= "false";

	private final List<AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>>	numericParsers;

	public LiteralParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
		numericParsers = ImmutableList.of(
			new NumericLiteralParser<>(parserToolbox, int.class,	TokenStream::readIntegerLiteral),
			new NumericLiteralParser<>(parserToolbox, long.class,	TokenStream::readLongLiteral),
			new NumericLiteralParser<>(parserToolbox, float.class,	TokenStream::readFloatLiteral),
			new NumericLiteralParser<>(parserToolbox, double.class,	TokenStream::readDoubleLiteral)
		);
	}

	@Override
	ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, InternalErrorException, AmbiguousParseResultException, InternalEvaluationException {
		String characters = tokenStream.peekCharacters();
		if (characters.length() == 0) {
			throw new InternalParseException("Expected a literal");
		}
		char firstCharacter = characters.charAt(0);
		switch (firstCharacter) {
			case '"':
				return parseStringLiteral(tokenStream);
			case '\'':
				return parseCharacterLiteral(tokenStream);
			case 'n':	// null
			case 't':	// this, true
			case 'f':	// false
				return parseNamedLiteral(tokenStream, expectation);
			case '+':
			case '-':
			case '.':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				return parseNumericLiteral(tokenStream, expectation);
			default:
				throw new InternalParseException("Expected a literal");
		}
	}

	private ObjectParseResult parseStringLiteral(TokenStream tokenStream) throws InternalParseException, CodeCompletionException, InternalErrorException {
		String stringLiteral = tokenStream.readStringLiteral();
		log(LogLevel.SUCCESS, "detected string literal '" + stringLiteral + "'");
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		ObjectInfo stringLiteralInfo = InfoProvider.createObjectInfo(stringLiteral, InfoProvider.createTypeInfo(String.class));
		return isCompile()
				? ParseResults.createCompiledConstantObjectParseResult(stringLiteralInfo)
				: ParseResults.createObjectParseResult(stringLiteralInfo);
	}

	private ObjectParseResult parseCharacterLiteral(TokenStream tokenStream) throws InternalParseException, CodeCompletionException, InternalErrorException {
		char characterLiteral = tokenStream.readCharacterLiteral();
		log(LogLevel.SUCCESS, "detected character literal '" + characterLiteral + "'");
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		ObjectInfo characterLiteralInfo = InfoProvider.createObjectInfo(characterLiteral, InfoProvider.createTypeInfo(char.class));
		return isCompile()
				? ParseResults.createCompiledConstantObjectParseResult(characterLiteralInfo)
				: ParseResults.createObjectParseResult(characterLiteralInfo);
	}

	private ObjectParseResult parseNamedLiteral(TokenStream tokenStream, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, InternalErrorException {
		String literal = tokenStream.readKeyword(info -> suggestNamedLiteral(info, expectation), "Expected a named literal");
		Map<String, ObjectInfo> namedLiteralMap = createNamedLiteralMap();
		ObjectInfo literalInfo = namedLiteralMap.get(literal);
		if (literalInfo == null) {
			throw new InternalParseException("Unknown literal '" + literal + "'");
		}
		log(LogLevel.SUCCESS, "detected literal '" + literal + "'");
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		if (!isCompile()) {
			return ParseResults.createObjectParseResult(literalInfo);
		}
		// "this" is not a constant literal, but depends on the context
		return THIS_LITERAL.equals(literal)
				? new CompiledThisParseResult(literalInfo)
				: ParseResults.createCompiledConstantObjectParseResult(literalInfo);
	}

	private CodeCompletions suggestNamedLiteral(CompletionInfo info, ObjectParseResultExpectation expectation) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting named literals");

		Map<String, ObjectInfo> literalMap = createNamedLiteralMap();
		ImmutableList.Builder<CodeCompletion> completionsBuilder = ImmutableList.builder();
		for (String literal : literalMap.keySet()) {
			if (!literal.startsWith(nameToComplete)) {
				continue;
			}
			ObjectInfo literalInfo = literalMap.get(literal);
			TypeInfo literalType = parserToolbox.getObjectInfoProvider().getType(literalInfo);
			StringMatch stringMatch = MatchRatings.rateStringMatch(literal, nameToComplete);
			TypeMatch typeMatch = expectation.rateTypeMatch(literalType);
			MatchRating matchRating = MatchRatings.create(stringMatch, typeMatch, false);
			CodeCompletion completion = CodeCompletionFactory.keywordCompletion(literal, insertionBegin, insertionEnd, matchRating);
			completionsBuilder.add(completion);
		}
		return new CodeCompletions(completionsBuilder.build());
	}

	private Map<String, ObjectInfo> createNamedLiteralMap() {
		return ImmutableMap.of(
			NULL_LITERAL,	InfoProvider.NULL_LITERAL,
			FALSE_LITERAL,	FALSE_INFO,
			TRUE_LITERAL,	TRUE_INFO,
			THIS_LITERAL,	parserToolbox.getThisInfo()
		);
	}

	private ObjectParseResult parseNumericLiteral(TokenStream tokenStream, ObjectParseResultExpectation expectation) throws CodeCompletionException, InternalErrorException, AmbiguousParseResultException, InternalParseException, InternalEvaluationException {
		ObjectParseResult parseResult = ParseUtils.parse(tokenStream, null, expectation, numericParsers);
		increaseConfidence(ParserConfidence.RIGHT_PARSER);
		return parseResult;
	}

	private static class CompiledThisParseResult extends AbstractCompiledParseResult
	{
		CompiledThisParseResult(ObjectInfo thisInfo) {
			super(thisInfo);
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			return thisInfo;
		}
	}

	private static class NumericLiteralParser<V> extends AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>
	{
		private final TypeInfo				numericType;
		private final NumericTokenReader<V>	tokenReader;

		NumericLiteralParser(ParserToolbox parserToolbox, Class<?> numericType, NumericTokenReader<V> tokenReader) {
			super(parserToolbox);
			this.numericType = InfoProvider.createTypeInfo(numericType);
			this.tokenReader = tokenReader;
		}

		@Override
		ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, InternalErrorException {
			V literalValue = tokenReader.read(tokenStream);
			log(LogLevel.SUCCESS, "detected numeric literal '" + literalValue + "'");
			increaseConfidence(ParserConfidence.RIGHT_PARSER);
			ObjectInfo literalInfo = InfoProvider.createObjectInfo(literalValue, numericType);
			return isCompile()
					? ParseResults.createCompiledConstantObjectParseResult(literalInfo)
					: ParseResults.createObjectParseResult(literalInfo);
		}
	}

	@FunctionalInterface
	private interface NumericTokenReader<T>
	{
		T read(TokenStream tokenStream) throws InternalParseException, CodeCompletionException;
	}
}
