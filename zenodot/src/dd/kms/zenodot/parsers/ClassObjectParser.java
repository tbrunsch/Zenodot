package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.flowcontrol.InternalErrorException;
import dd.kms.zenodot.flowcontrol.SyntaxException;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.matching.TypeMatch;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.AbstractObjectParseResult;
import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.result.codecompletions.CodeCompletionFactory;
import dd.kms.zenodot.tokenizer.CompletionInfo;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * Parses subexpressions {@code class} of expressions of the form {@code <class>.class}.
 * The class {@code <class>} is the context for the parser.
 */
public class ClassObjectParser extends AbstractParserWithObjectTail<TypeInfo>
{
	private static final String	CLASS_KEYWORD	= "class";
	private static final String	ERROR_MESSAGE	= "Expected keyword '" + CLASS_KEYWORD + "'";

	public ClassObjectParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ObjectParseResult parseNext(TokenStream tokenStream, TypeInfo contextType, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException {
		String keyword = tokenStream.readKeyword(this::suggestClassKeyword, ERROR_MESSAGE);
		if (!CLASS_KEYWORD.equals(keyword)) {
			throw new SyntaxException(ERROR_MESSAGE);
		}
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		Class<?> classObject = contextType.getRawType();
		ObjectInfo classObjectInfo = InfoProvider.createObjectInfo(classObject, InfoProvider.createTypeInfo(Class.class));

		return new ClassObjectParseResult(classObject, classObjectInfo, tokenStream.getPosition());
	}

	private CodeCompletions suggestClassKeyword(CompletionInfo info) throws SyntaxException {
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

	private static class ClassObjectParseResult extends AbstractObjectParseResult
	{
		private final Class<?>	classObject;

		ClassObjectParseResult(Class<?> classObject, ObjectInfo classObjectInfo, int position) {
			super(classObjectInfo, position);
			this.classObject = classObject;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) {
			return InfoProvider.createObjectInfo(classObject, InfoProvider.createTypeInfo(Class.class));
		}
	}
}
