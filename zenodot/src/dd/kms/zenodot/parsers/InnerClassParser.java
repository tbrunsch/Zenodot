package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.result.ClassParseResult;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.tokenizer.CompletionInfo;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataproviders.ClassDataProvider;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Arrays;
import java.util.Optional;

/**
 * Parses subexpressions {@code <inner class>} of expressions of the form {@code <class>.<inner class>}.
 * The class {@code <class>} is the context for the parser.
 */
public class InnerClassParser<T extends ParseResult, S extends ParseResultExpectation<T>> extends AbstractParser<TypeInfo, T, S>
{
	public InnerClassParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ParseResult doParse(TokenStream tokenStream, TypeInfo contextType, S expectation) throws AmbiguousParseResultException, CodeCompletionException, InternalParseException, InternalEvaluationException, InternalErrorException {
		ClassParseResult innerClassParseResult = readInnerClass(tokenStream, contextType);
		TypeInfo innerClassType = innerClassParseResult.getType();

		return parserToolbox.createParser(ClassTailParser.class).parse(tokenStream, innerClassType, expectation);
	}

	private ClassParseResult readInnerClass(TokenStream tokenStream, TypeInfo contextType) throws InternalParseException, CodeCompletionException, InternalErrorException {
		Class<?> contextClass = contextType.getRawType();
		String innerClassName = tokenStream.readIdentifier(info -> suggestInnerClasses(contextType, info), "Expected an inner class name");

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		Optional<Class<?>> firstClassMatch = Arrays.stream(contextClass.getDeclaredClasses())
			.filter(clazz -> clazz.getSimpleName().equals(innerClassName))
			.findFirst();
		if (!firstClassMatch.isPresent()) {
			throw new InternalParseException("Unknown inner class '" + innerClassName + "'");
		}

		log(LogLevel.SUCCESS, "detected inner class '" + innerClassName + "'");

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		Class<?> innerClass = firstClassMatch.get();
		TypeInfo innerClassType = contextType.resolveType(innerClass);
		return ParseResults.createClassParseResult(innerClassType);
	}

	private CodeCompletions suggestInnerClasses(TypeInfo contextType, CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting inner classes matching '" + nameToComplete + "'");

		ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();
		Class<?> contextClass = contextType.getRawType();
		return classDataProvider.completeInnerClass("", contextClass, insertionBegin, insertionEnd);
	}
}
