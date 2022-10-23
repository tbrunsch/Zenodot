package dd.kms.zenodot.impl.parsers;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ObjectParseResult;
import dd.kms.zenodot.impl.wrappers.InfoProvider;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.matching.MatchRatings;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.result.AbstractObjectParseResult;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.impl.result.ParseResults;
import dd.kms.zenodot.impl.result.codecompletions.CodeCompletionFactory;
import dd.kms.zenodot.impl.tokenizer.CompletionInfo;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.utils.ParserToolbox;

import java.util.HashMap;
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
	private static final ObjectInfo	TRUE_INFO	= InfoProvider.createObjectInfo(true, boolean.class);
	private static final ObjectInfo	FALSE_INFO	= InfoProvider.createObjectInfo(false, boolean.class);

	private static final String		NULL_LITERAL	= "null";
	private static final String		THIS_LITERAL	= "this";
	private static final String		TRUE_LITERAL	= "true";
	private static final String		FALSE_LITERAL	= "false";

	private final List<AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>>	numericParsers;

	public LiteralParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
		numericParsers = ImmutableList.of(
			new NumericLiteralParser<>(parserToolbox, TokenStream::readIntegerLiteral),
			new NumericLiteralParser<>(parserToolbox, TokenStream::readLongLiteral),
			new NumericLiteralParser<>(parserToolbox, TokenStream::readFloatLiteral),
			new NumericLiteralParser<>(parserToolbox, TokenStream::readDoubleLiteral)
		);
	}

	@Override
	ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		char c = tokenStream.peekCharacter();
		switch (c) {
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
		increaseConfidence(ParserConfidence.RIGHT_PARSER);
		String stringLiteral = tokenStream.readStringLiteral();
		log(LogLevel.SUCCESS, "detected string literal '" + stringLiteral + "'");

		return ParseResults.createCompiledConstantObjectParseResult(stringLiteral, tokenStream);
	}

	private ObjectParseResult parseCharacterLiteral(TokenStream tokenStream) throws SyntaxException, CodeCompletionException, InternalErrorException {
		increaseConfidence(ParserConfidence.RIGHT_PARSER);
		char characterLiteral = tokenStream.readCharacterLiteral();
		log(LogLevel.SUCCESS, "detected character literal '" + characterLiteral + "'");

		return ParseResults.createCompiledConstantObjectParseResult(characterLiteral, tokenStream);
	}

	private ObjectParseResult parseNamedLiteral(TokenStream tokenStream, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException {
		String literal = tokenStream.readKeyword(info -> suggestNamedLiteral(info, expectation), "Expected a named literal");
		Map<String, Object> namedLiteralMap = createNamedLiteralMap();
		if (!namedLiteralMap.containsKey(literal)) {
			throw new SyntaxException("Unknown literal '" + literal + "'");
		}
		Object literalValue = namedLiteralMap.get(literal);
		log(LogLevel.SUCCESS, "detected literal '" + literal + "'");
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		// "this" is not a constant literal, but depends on the context
		return THIS_LITERAL.equals(literal)
				? new ThisParseResult(literalValue, tokenStream)
				: ParseResults.createCompiledConstantObjectParseResult(literalValue, tokenStream);
	}

	private CodeCompletions suggestNamedLiteral(CompletionInfo info, ObjectParseResultExpectation expectation) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting named literals");

		Map<String, Object> literalMap = createNamedLiteralMap();
		ImmutableList.Builder<CodeCompletion> completionsBuilder = ImmutableList.builder();
		for (String literal : literalMap.keySet()) {
			if (!literal.startsWith(nameToComplete)) {
				continue;
			}
			Object literalValue = literalMap.get(literal);
			Class<?> literalType = literalValue != null ? literalValue.getClass() : InfoProvider.NO_TYPE;
			StringMatch stringMatch = MatchRatings.rateStringMatch(literal, nameToComplete);
			TypeMatch typeMatch = expectation.rateTypeMatch(literalType);
			MatchRating matchRating = MatchRatings.create(stringMatch, typeMatch, false);
			CodeCompletion completion = CodeCompletionFactory.keywordCompletion(literal, insertionBegin, insertionEnd, matchRating);
			completionsBuilder.add(completion);
		}
		return new CodeCompletions(completionsBuilder.build());
	}

	private Map<String, Object> createNamedLiteralMap() {
		Map<String, Object> namedLiteralMap = new HashMap<>();
		namedLiteralMap.put(NULL_LITERAL,	null);
		namedLiteralMap.put(FALSE_LITERAL,	false);
		namedLiteralMap.put(TRUE_LITERAL,	true);
		namedLiteralMap.put(THIS_LITERAL,	parserToolbox.getThisInfo().getObject());
		return namedLiteralMap;
	}

	private ObjectParseResult parseNumericLiteral(TokenStream tokenStream, ObjectParseResultExpectation expectation) throws CodeCompletionException, InternalErrorException, SyntaxException, EvaluationException {
		ObjectParseResult parseResult = ParseUtils.parse(tokenStream, null, expectation, numericParsers);
		increaseConfidence(ParserConfidence.RIGHT_PARSER);
		return parseResult;
	}

	private static class ThisParseResult extends AbstractObjectParseResult
	{
		ThisParseResult(Object thisValue, TokenStream tokenStream) {
			super(InfoProvider.createObjectInfo(thisValue), tokenStream);
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) {
			return thisInfo;
		}
	}

	private static class NumericLiteralParser<V> extends AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>
	{
		private final NumericTokenReader<V>	tokenReader;

		NumericLiteralParser(ParserToolbox parserToolbox, NumericTokenReader<V> tokenReader) {
			super(parserToolbox);
			this.tokenReader = tokenReader;
		}

		@Override
		ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException {
			V literalValue = tokenReader.read(tokenStream);
			log(LogLevel.SUCCESS, "detected numeric literal '" + literalValue + "'");
			increaseConfidence(ParserConfidence.RIGHT_PARSER);
			return ParseResults.createCompiledConstantObjectParseResult(literalValue, tokenStream);
		}
	}

	@FunctionalInterface
	private interface NumericTokenReader<T>
	{
		T read(TokenStream tokenStream) throws SyntaxException, CodeCompletionException;
	}
}
