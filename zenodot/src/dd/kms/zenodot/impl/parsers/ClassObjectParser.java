package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.matching.MatchRatings;
import dd.kms.zenodot.framework.parsers.AbstractParserWithObjectTail;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.CodeCompletions;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.tokenizer.CompletionInfo;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.result.codecompletions.CodeCompletionFactory;

/**
 * Parses subexpressions {@code class} of expressions of the form {@code <class>.class}.
 * The class {@code <class>} is the context for the parser.
 */
public class ClassObjectParser extends AbstractParserWithObjectTail<Class<?>>
{
	private static final String	CLASS_KEYWORD	= "class";
	private static final String	ERROR_MESSAGE	= "Expected keyword '" + CLASS_KEYWORD + "'";

	public ClassObjectParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	protected ObjectParseResult parseNext(TokenStream tokenStream, Class<?> contextType, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException {
		String keyword = tokenStream.readKeyword(this::suggestClassKeyword, ERROR_MESSAGE);
		if (!CLASS_KEYWORD.equals(keyword)) {
			throw new SyntaxException(ERROR_MESSAGE);
		}
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		ObjectInfo classObjectInfo = InfoProvider.createObjectInfo(contextType, Class.class);

		return new ClassObjectParseResult(contextType, classObjectInfo, tokenStream);
	}

	private CodeCompletions suggestClassKeyword(CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		if (!CLASS_KEYWORD.startsWith(nameToComplete)) {
			return CodeCompletions.NONE;
		}
		log(LogLevel.SUCCESS, "suggesting keyword '" + CLASS_KEYWORD + "'");

		StringMatch stringMatch = MatchRatings.rateStringMatch(CLASS_KEYWORD, nameToComplete);
		MatchRating matchRating = MatchRatings.create(stringMatch, TypeMatch.NONE, false);
		CodeCompletion completion = CodeCompletionFactory.keywordCompletion(CLASS_KEYWORD, insertionBegin, insertionEnd, matchRating);
		return CodeCompletions.of(completion);
	}

	private static class ClassObjectParseResult extends ObjectParseResult
	{
		private final Class<?>	classObject;

		ClassObjectParseResult(Class<?> classObject, ObjectInfo classObjectInfo, TokenStream tokenStream) {
			super(classObjectInfo, tokenStream);
			this.classObject = classObject;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo, Variables variables) {
			return InfoProvider.createObjectInfo(classObject, Class.class);
		}
	}
}
