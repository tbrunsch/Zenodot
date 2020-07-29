package dd.kms.zenodot.impl.parsers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.impl.matching.MatchRatings;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.api.result.*;
import dd.kms.zenodot.impl.result.codecompletions.CodeCompletionFactory;
import dd.kms.zenodot.impl.result.AbstractObjectParseResult;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.impl.result.ParseResults;
import dd.kms.zenodot.impl.tokenizer.CompletionInfo;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.utils.ParserToolbox;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

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
	ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		String characters = tokenStream.peekCharacters();
		if (characters.length() == 0) {
			throw new SyntaxException("Expected a literal");
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
				throw new SyntaxException("Expected a literal");
		}
	}

	private ObjectParseResult parseStringLiteral(TokenStream tokenStream) throws SyntaxException, CodeCompletionException, InternalErrorException {
		String stringLiteral = tokenStream.readStringLiteral();
		log(LogLevel.SUCCESS, "detected string literal '" + stringLiteral + "'");
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		ObjectInfo stringLiteralInfo = InfoProvider.createObjectInfo(stringLiteral, InfoProvider.createTypeInfo(String.class));
		return ParseResults.createCompiledConstantObjectParseResult(stringLiteralInfo, tokenStream.getPosition());
	}

	private ObjectParseResult parseCharacterLiteral(TokenStream tokenStream) throws SyntaxException, CodeCompletionException, InternalErrorException {
		char characterLiteral = tokenStream.readCharacterLiteral();
		log(LogLevel.SUCCESS, "detected character literal '" + characterLiteral + "'");
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		ObjectInfo characterLiteralInfo = InfoProvider.createObjectInfo(characterLiteral, InfoProvider.createTypeInfo(char.class));
		return ParseResults.createCompiledConstantObjectParseResult(characterLiteralInfo, tokenStream.getPosition());
	}

	private ObjectParseResult parseNamedLiteral(TokenStream tokenStream, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException {
		String literal = tokenStream.readKeyword(info -> suggestNamedLiteral(info, expectation), "Expected a named literal");
		Map<String, ObjectInfo> namedLiteralMap = createNamedLiteralMap();
		ObjectInfo literalInfo = namedLiteralMap.get(literal);
		if (literalInfo == null) {
			throw new SyntaxException("Unknown literal '" + literal + "'");
		}
		log(LogLevel.SUCCESS, "detected literal '" + literal + "'");
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		// "this" is not a constant literal, but depends on the context
		return THIS_LITERAL.equals(literal)
				? new ThisParseResult(literalInfo, tokenStream.getPosition())
				: ParseResults.createCompiledConstantObjectParseResult(literalInfo, tokenStream.getPosition());
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

	private ObjectParseResult parseNumericLiteral(TokenStream tokenStream, ObjectParseResultExpectation expectation) throws CodeCompletionException, InternalErrorException, SyntaxException, EvaluationException {
		ObjectParseResult parseResult = ParseUtils.parse(tokenStream, null, expectation, numericParsers);
		increaseConfidence(ParserConfidence.RIGHT_PARSER);
		return parseResult;
	}

	private static class ThisParseResult extends AbstractObjectParseResult
	{
		ThisParseResult(ObjectInfo thisInfo, int position) {
			super(thisInfo, position);
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) {
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
		ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException {
			V literalValue = tokenReader.read(tokenStream);
			log(LogLevel.SUCCESS, "detected numeric literal '" + literalValue + "'");
			increaseConfidence(ParserConfidence.RIGHT_PARSER);
			ObjectInfo literalInfo = InfoProvider.createObjectInfo(literalValue, numericType);
			return ParseResults.createCompiledConstantObjectParseResult(literalInfo, tokenStream.getPosition());
		}
	}

	@FunctionalInterface
	private interface NumericTokenReader<T>
	{
		T read(TokenStream tokenStream) throws SyntaxException, CodeCompletionException;
	}
}
