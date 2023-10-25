package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.framework.result.ClassParseResult;
import dd.kms.zenodot.framework.result.CodeCompletions;
import dd.kms.zenodot.framework.result.ParseResult;
import dd.kms.zenodot.framework.result.ParseResults;
import dd.kms.zenodot.framework.tokenizer.CompletionInfo;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.impl.utils.dataproviders.ClassDataProvider;

import java.util.Arrays;
import java.util.Optional;

/**
 * Parses subexpressions {@code <inner class>} of expressions of the form {@code <class>.<inner class>}.
 * The class {@code <class>} is the context for the parser.
 */
public class InnerClassParser<T extends ParseResult, S extends ParseResultExpectation<T>> extends AbstractParser<Class<?>, T, S>
{
	public InnerClassParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	protected ParseResult doParse(TokenStream tokenStream, Class<?> contextType, S expectation) throws CodeCompletionException, SyntaxException, EvaluationException, InternalErrorException {
		ClassParseResult innerClassParseResult = readInnerClass(tokenStream, contextType);
		Class<?> innerClassType = innerClassParseResult.getType();

		return parserToolbox.createParser(ClassTailParser.class).parse(tokenStream, innerClassType, expectation);
	}

	private ClassParseResult readInnerClass(TokenStream tokenStream, Class<?> contextType) throws SyntaxException, CodeCompletionException, InternalErrorException {
		String innerClassName = tokenStream.readIdentifier(info -> suggestInnerClasses(contextType, info), "Expected an inner class name");

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		Optional<Class<?>> firstClassMatch = Arrays.stream(contextType.getDeclaredClasses())
			.filter(clazz -> clazz.getSimpleName().equals(innerClassName))
			.findFirst();
		if (!firstClassMatch.isPresent()) {
			throw new SyntaxException("Unknown inner class '" + innerClassName + "'");
		}

		log(LogLevel.SUCCESS, "detected inner class '" + innerClassName + "'");

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		Class<?> innerClass = firstClassMatch.get();
		return ParseResults.createClassParseResult(innerClass);
	}

	private CodeCompletions suggestInnerClasses(Class<?> contextType, CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting inner classes matching '" + nameToComplete + "'");

		return ClassDataProvider.completeInnerClass(nameToComplete, contextType, insertionBegin, insertionEnd);
	}
}
