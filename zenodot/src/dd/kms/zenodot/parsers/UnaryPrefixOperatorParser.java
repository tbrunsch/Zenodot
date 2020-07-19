package dd.kms.zenodot.parsers;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.AbstractCompiledParseResult;
import dd.kms.zenodot.result.CompiledObjectParseResult;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.tokenizer.UnaryOperator;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataproviders.OperatorResultProvider;
import dd.kms.zenodot.utils.dataproviders.OperatorResultProvider.OperatorException;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.Map;

/**
 * Parses prefix unary operators applied to an expression in the context of {@code this}. The following
 * expressions can currently be parsed:
 * <ul>
 *     <li>{@code ++<expression>}</li>
 *     <li>{@code --<expression>}</li>
 *     <li>{@code +<expression>}</li>
 *     <li>{@code -<expression>}</li>
 *     <li>{@code !<expression>}</li>
 *     <li>{@code ~<expression>}</li>
 * </ul>
 * Note that the postfix operators {@code <expression>++} and {@code <expression>--} are not supported.
 */
public class UnaryPrefixOperatorParser extends AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>
{
	private static final Map<UnaryOperator, OperatorImplementation>	OPERATOR_IMPLEMENTATIONS = ImmutableMap.<UnaryOperator, OperatorImplementation>builder()
		.put(UnaryOperator.INCREMENT, 	OperatorResultProvider::getIncrementInfo)
		.put(UnaryOperator.DECREMENT, 	OperatorResultProvider::getDecrementInfo)
		.put(UnaryOperator.PLUS, 		OperatorResultProvider::getPlusInfo)
		.put(UnaryOperator.MINUS, 		OperatorResultProvider::getMinusInfo)
		.put(UnaryOperator.LOGICAL_NOT,	OperatorResultProvider::getLogicalNotInfo)
		.put(UnaryOperator.BITWISE_NOT,	OperatorResultProvider::getBitwiseNotInfo)
		.build();

	public UnaryPrefixOperatorParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalEvaluationException, InternalErrorException {
		UnaryOperator operator = tokenStream.readUnaryOperator();
		if (operator == null) {
			throw new InternalParseException("Expression does not start with an unary operator");
		}

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		ObjectParseResult expressionParseResult = parserToolbox.createParser(SimpleExpressionParser.class).parse(tokenStream, contextInfo, expectation);

		if (isCompile()) {
			return new CompiledUnaryPrefixOperatorParseResult((CompiledObjectParseResult) expressionParseResult, operator);
		}

		ObjectInfo expressionInfo = expressionParseResult.getObjectInfo();
		ObjectInfo operatorResult;
		try {
			operatorResult = applyOperator(expressionInfo, operator);
			log(LogLevel.SUCCESS, "applied operator successfully");
		} catch (OperatorException e) {
			throw new InternalEvaluationException("Applying operator failed: " + e.getMessage(), e);
		}
		return ParseResults.createObjectParseResult(operatorResult);
	}

	private ObjectInfo applyOperator(ObjectInfo objectInfo, UnaryOperator operator) throws OperatorException {
		return applyOperator(objectInfo, operator, parserToolbox.getOperatorResultProvider());
	}

	private static ObjectInfo applyOperator(ObjectInfo objectInfo, UnaryOperator operator, OperatorResultProvider operatorResultProvider) throws OperatorException {
		return OPERATOR_IMPLEMENTATIONS.get(operator).apply(operatorResultProvider, objectInfo);
	}

	@FunctionalInterface
	private interface OperatorImplementation
	{
		ObjectInfo apply(OperatorResultProvider operatorResultProvider, ObjectInfo objectInfo) throws OperatorException;
	}

	private static class CompiledUnaryPrefixOperatorParseResult extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult expressionParseResult;
		private final UnaryOperator				operator;

		private CompiledUnaryPrefixOperatorParseResult(CompiledObjectParseResult expressionParseResult, UnaryOperator operator) {
			super(expressionParseResult.getObjectInfo());
			this.expressionParseResult = expressionParseResult;
			this.operator = operator;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			ObjectInfo expressionInfo = expressionParseResult.evaluate(thisInfo, contextInfo);
			return applyOperator(expressionInfo, operator, OPERATOR_RESULT_PROVIDER);
		}
	}
}
