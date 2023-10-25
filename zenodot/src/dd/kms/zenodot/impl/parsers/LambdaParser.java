package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParseUtils;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses expressions of the form {@code x -> x*x} or {@code (x, y) -> x + y} in the context of {@code this}.
 */
public class LambdaParser extends AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>
{
	public LambdaParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	protected ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo context, ObjectParseResultExpectation expectation) throws CodeCompletionException, SyntaxException, InternalErrorException, EvaluationException {
		List<Class<?>> expectedTypes = expectation.getExpectedTypes();
		if (expectedTypes == null) {
			throw new SyntaxException("No functional interface expected");
		}
		List<Class<?>> possibleFunctionalInterfaces = expectedTypes.stream()
			.filter(ReflectionUtils::isFunctionalInterface)
			.collect(Collectors.toList());
		if (possibleFunctionalInterfaces.isEmpty()) {
			throw new SyntaxException("No functional interface expected");
		}

		List<ConcreteLambdaParser> parsers = possibleFunctionalInterfaces.stream()
			.map(functionalInterface -> new ConcreteLambdaParser(parserToolbox, functionalInterface))
			.collect(Collectors.toList());

		ObjectParseResult parseResult;

		try {
			parseResult = ParseUtils.parse(tokenStream, context, expectation, parsers);
		} catch (SyntaxException e) {
			ParserConfidence confidence = parsers.stream().map(AbstractParser::getConfidence)
				.max(Comparator.comparing(ParserConfidence::ordinal))
				.orElseThrow(() -> new IllegalStateException("No parsers found. This should not happen because this has been checked before."));
			increaseConfidence(confidence);
			throw e;
		}

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		return parseResult;
	}
}
