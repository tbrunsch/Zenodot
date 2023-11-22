package dd.kms.zenodot.impl.parsers;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Primitives;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.matching.MatchRatings;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.AbstractParserWithObjectTail;
import dd.kms.zenodot.framework.parsers.CallerContext;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.CodeCompletions;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.result.ParseResults;
import dd.kms.zenodot.framework.tokenizer.CompletionGenerator;
import dd.kms.zenodot.framework.tokenizer.CompletionInfo;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParseUtils;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.result.codecompletions.CodeCompletionFactory;

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
	protected ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
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
		ParserToolbox parserToolbox = getToolbox();
		CallerContext callerContext = getCallerContext();
		CompletionGenerator completionGenerator = parserToolbox.getStringLiteralCompletionGenerator(callerContext);
		String stringLiteral = tokenStream.readStringLiteral(completionGenerator);
		log(LogLevel.SUCCESS, "detected string literal '" + stringLiteral + "'");

		return ParseResults.createCompiledConstantObjectParseResult(InfoProvider.createObjectInfo(stringLiteral), tokenStream);
	}

	private ObjectParseResult parseCharacterLiteral(TokenStream tokenStream) throws SyntaxException, CodeCompletionException, InternalErrorException {
		increaseConfidence(ParserConfidence.RIGHT_PARSER);
		char characterLiteral = tokenStream.readCharacterLiteral();
		log(LogLevel.SUCCESS, "detected character literal '" + characterLiteral + "'");

		return ParseResults.createCompiledConstantObjectParseResult(InfoProvider.createObjectInfo(characterLiteral, char.class), tokenStream);
	}

	private ObjectParseResult parseNamedLiteral(TokenStream tokenStream, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException {
		String literal = tokenStream.readKeyword(info -> suggestNamedLiteral(info, expectation), "Expected a named literal");
		Map<String, ObjectInfo> namedLiteralMap = createNamedLiteralMap();
		if (!namedLiteralMap.containsKey(literal)) {
			throw new SyntaxException("Unknown literal '" + literal + "'");
		}
		ObjectInfo literalValueInfo = namedLiteralMap.get(literal);
		log(LogLevel.SUCCESS, "detected literal '" + literal + "'");
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		// "this" is not a constant literal, but depends on the context
		return THIS_LITERAL.equals(literal)
				? new ThisParseResult(literalValueInfo, tokenStream)
				: ParseResults.createCompiledConstantObjectParseResult(literalValueInfo, tokenStream);
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
			ObjectInfo literalValueInfo = literalMap.get(literal);
			Class<?> literalType = literalValueInfo.getDeclaredType();
			StringMatch stringMatch = MatchRatings.rateStringMatch(literal, nameToComplete);
			TypeMatch typeMatch = expectation.rateTypeMatch(literalType);
			MatchRating matchRating = MatchRatings.create(stringMatch, typeMatch, false);
			CodeCompletion completion = CodeCompletionFactory.keywordCompletion(literal, insertionBegin, insertionEnd, matchRating);
			completionsBuilder.add(completion);
		}
		return new CodeCompletions(completionsBuilder.build());
	}

	private Map<String, ObjectInfo> createNamedLiteralMap() {
		Map<String, ObjectInfo> namedLiteralMap = new HashMap<>();
		namedLiteralMap.put(NULL_LITERAL,	InfoProvider.NULL_LITERAL);
		namedLiteralMap.put(FALSE_LITERAL,	InfoProvider.createObjectInfo(false, boolean.class));
		namedLiteralMap.put(TRUE_LITERAL,	InfoProvider.createObjectInfo(true, boolean.class));
		namedLiteralMap.put(THIS_LITERAL,	parserToolbox.getThisInfo());
		return namedLiteralMap;
	}

	private ObjectParseResult parseNumericLiteral(TokenStream tokenStream, ObjectParseResultExpectation expectation) throws CodeCompletionException, InternalErrorException, SyntaxException, EvaluationException {
		ObjectParseResult parseResult = ParseUtils.parse(tokenStream, null, expectation, numericParsers);
		increaseConfidence(ParserConfidence.RIGHT_PARSER);
		return parseResult;
	}

	private static class ThisParseResult extends ObjectParseResult
	{
		ThisParseResult(ObjectInfo thisValueInfo, TokenStream tokenStream) {
			super(thisValueInfo, tokenStream);
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo, Variables variables) {
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
		protected ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException {
			V literalValue = tokenReader.read(tokenStream);
			log(LogLevel.SUCCESS, "detected numeric literal '" + literalValue + "'");
			increaseConfidence(ParserConfidence.RIGHT_PARSER);
			return ParseResults.createCompiledConstantObjectParseResult(InfoProvider.createObjectInfo(literalValue, Primitives.unwrap(literalValue.getClass())), tokenStream);
		}
	}

	@FunctionalInterface
	private interface NumericTokenReader<T>
	{
		T read(TokenStream tokenStream) throws SyntaxException, CodeCompletionException;
	}
}
