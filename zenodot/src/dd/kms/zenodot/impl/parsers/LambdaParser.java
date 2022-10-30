package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.result.ObjectParseResult;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.utils.ParserToolbox;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

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
	ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo context, ObjectParseResultExpectation expectation) throws CodeCompletionException, SyntaxException, InternalErrorException, EvaluationException {
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
