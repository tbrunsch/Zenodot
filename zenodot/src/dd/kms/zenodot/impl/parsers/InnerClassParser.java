package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.impl.result.ClassParseResult;
import dd.kms.zenodot.impl.result.ParseResult;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.impl.result.ParseResults;
import dd.kms.zenodot.impl.tokenizer.CompletionInfo;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParserToolbox;
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
	ParseResult doParse(TokenStream tokenStream, Class<?> contextType, S expectation) throws CodeCompletionException, SyntaxException, EvaluationException, InternalErrorException {
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

		ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();
		return classDataProvider.completeInnerClass(nameToComplete, contextType, insertionBegin, insertionEnd);
	}
}
