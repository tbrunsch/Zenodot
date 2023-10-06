package dd.kms.zenodot.impl.parsers;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.operators.UnaryOperator;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.utils.dataproviders.OperatorResultProvider;
import dd.kms.zenodot.impl.utils.dataproviders.OperatorResultProvider.OperatorException;

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
	protected ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, EvaluationException, InternalErrorException {
		UnaryOperator operator = tokenStream.readUnaryOperator(TokenStream.NO_COMPLETIONS);
		if (operator == null) {
			throw new SyntaxException("Expression does not start with an unary operator");
		}

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		ObjectParseResult expressionParseResult = parserToolbox.createParser(SimpleExpressionParser.class).parse(tokenStream, contextInfo, expectation);

		ObjectInfo expressionInfo = expressionParseResult.getObjectInfo();
		ObjectInfo operatorResult;
		try {
			operatorResult = applyOperator(expressionInfo, operator);
			log(LogLevel.SUCCESS, "applied operator successfully");
		} catch (OperatorException e) {
			throw new EvaluationException("Applying operator failed: " + e.getMessage(), e);
		}
		return new UnaryPrefixOperatorParseResult(expressionParseResult, operator, operatorResult, tokenStream);
	}

	private ObjectInfo applyOperator(ObjectInfo objectInfo, UnaryOperator operator) throws OperatorException {
		return applyOperator(objectInfo, operator, parserToolbox.inject(OperatorResultProvider.class));
	}

	private static ObjectInfo applyOperator(ObjectInfo objectInfo, UnaryOperator operator, OperatorResultProvider operatorResultProvider) throws OperatorException {
		return OPERATOR_IMPLEMENTATIONS.get(operator).apply(operatorResultProvider, objectInfo);
	}

	@FunctionalInterface
	private interface OperatorImplementation
	{
		ObjectInfo apply(OperatorResultProvider operatorResultProvider, ObjectInfo objectInfo) throws OperatorException;
	}

	private static class UnaryPrefixOperatorParseResult extends ObjectParseResult
	{
		private final ObjectParseResult expressionParseResult;
		private final UnaryOperator		operator;

		private UnaryPrefixOperatorParseResult(ObjectParseResult expressionParseResult, UnaryOperator operator, ObjectInfo operatorResult, TokenStream tokenStream) {
			super(operatorResult, tokenStream);
			this.expressionParseResult = expressionParseResult;
			this.operator = operator;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo, Variables variables) throws ParseException {
			ObjectInfo expressionInfo = expressionParseResult.evaluate(thisInfo, contextInfo, variables);
			try {
				return applyOperator(expressionInfo, operator, OPERATOR_RESULT_PROVIDER);
			} catch (OperatorException e) {
				throw new ParseException(getExpression(), getPosition(), "Exception when evaluating operator '" + operator + "': " + e.getMessage(), e);
			}
		}
	}
}
