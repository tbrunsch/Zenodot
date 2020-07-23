package dd.kms.zenodot.parsers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.AbstractObjectParseResult;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.Associativity;
import dd.kms.zenodot.tokenizer.BinaryOperator;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataproviders.OperatorResultProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dd.kms.zenodot.utils.dataproviders.OperatorResultProvider.OperatorException;

/**
 * Parses arbitrary Java expressions including binary operators by using the
 * {@link SimpleExpressionParser} for parsing the operands.
 */
public class ExpressionParser extends AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>
{
	private static final Map<BinaryOperator, OperatorImplementation>	OPERATOR_IMPLEMENTATIONS = ImmutableMap.<BinaryOperator, OperatorImplementation>builder()
		.put(BinaryOperator.MULTIPLY, 					OperatorResultProvider::getMultiplicationInfo)
		.put(BinaryOperator.DIVIDE, 					OperatorResultProvider::getDivisionInfo)
		.put(BinaryOperator.MODULO,						OperatorResultProvider::getModuloInfo)
		.put(BinaryOperator.ADD_OR_CONCAT, 				OperatorResultProvider::getAddOrConcatInfo)
		.put(BinaryOperator.SUBTRACT, 					OperatorResultProvider::getSubtractionInfo)
		.put(BinaryOperator.LEFT_SHIFT,					OperatorResultProvider::getLeftShiftInfo)
		.put(BinaryOperator.RIGHT_SHIFT, 				OperatorResultProvider::getRightShiftInfo)
		.put(BinaryOperator.UNSIGNED_RIGHT_SHIFT, 		OperatorResultProvider::getUnsignedRightShiftInfo)
		.put(BinaryOperator.LESS_THAN, 					OperatorResultProvider::getLessThanInfo)
		.put(BinaryOperator.LESS_THAN_OR_EQUAL_TO,		OperatorResultProvider::getLessThanOrEqualToInfo)
		.put(BinaryOperator.GREATER_THAN, 				OperatorResultProvider::getGreaterThanInfo)
		.put(BinaryOperator.GREATER_THAN_OR_EQUAL_TO,	OperatorResultProvider::getGreaterThanOrEqualToInfo)
		.put(BinaryOperator.EQUAL_TO, 					OperatorResultProvider::getEqualToInfo)
		.put(BinaryOperator.NOT_EQUAL_TO, 				OperatorResultProvider::getNotEqualToInfo)
		.put(BinaryOperator.BITWISE_AND, 				OperatorResultProvider::getBitwiseAndInfo)
		.put(BinaryOperator.BITWISE_XOR, 				OperatorResultProvider::getBitwiseXorInfo)
		.put(BinaryOperator.BITWISE_OR, 				OperatorResultProvider::getBitwiseOrInfo)
		.put(BinaryOperator.LOGICAL_AND, 				OperatorResultProvider::getLogicalAndInfo)
		.put(BinaryOperator.LOGICAL_OR, 				OperatorResultProvider::getLogicalOrInfo)
		.put(BinaryOperator.ASSIGNMENT, 				OperatorResultProvider::getAssignmentInfo)
		.build();

	private final int maxOperatorPrecedenceLevelToConsider;

	public ExpressionParser(ParserToolbox parserToolbox, int maxOperatorPrecedenceLevelToConsider) {
		super(parserToolbox);
		this.maxOperatorPrecedenceLevelToConsider = maxOperatorPrecedenceLevelToConsider;
	}

	@Override
	ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws AmbiguousParseResultException, CodeCompletionException, InternalParseException, InternalEvaluationException, InternalErrorException {
		ObjectParseResultExpectation operandExpectation = expectation.parseWholeText(false).resultTypeMustMatch(false);

		List<ObjectParseResult> operands = new ArrayList<>();
		List<BinaryOperator> operators = new ArrayList<>();

		log(LogLevel.INFO, "parsing first operand");
		ObjectParseResult firstOperand = parserToolbox.createParser(SimpleExpressionParser.class).parse(tokenStream, contextInfo, operandExpectation);
		ObjectInfo accumulatedResultInfo = firstOperand.getObjectInfo();
		operands.add(firstOperand);

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		/*
		 * If short circuit evaluation becomes active, then we must switch to a non-evaluating contextInfo,
		 * but still check for syntax errors.
		 */
		ParserToolbox parserToolbox = this.parserToolbox;
		boolean considerOperatorResult = true;
		while (true) {
			BinaryOperator operator = tokenStream.peekBinaryOperator();
			if (operator == null || operator.getPrecedenceLevel() > maxOperatorPrecedenceLevelToConsider) {
				// Example: parsed "+" in "a*b + c" => only evaluate "a*b" and let outer expression parser evaluate sum of a*b and c
				if (operator != null) {
					log(LogLevel.SUCCESS, "detected binary operator '" + operator + "' at " + tokenStream);
				}

				return new ExpressionParseResult(operands, operators, accumulatedResultInfo, tokenStream.getPosition());
			}

			tokenStream.readBinaryOperator();
			operators.add(operator);

			/*
			 * We use an expression parser to parse the next operand. This can also be a compound expression,
			 * so we use the expression parser and must specify an operator precedence level.
			 *
			 * Case 1: left-to-right associative operator: The next operator must only contain operators
			 * of higher precedence (= lower precedence level). Example: Parsed "+" in "a + b*c + d". Now
			 * use the expression parser for evaluating b*c as second operand. Operators with the same
			 * precedence level as "+" or higher are not allowed.
			 *
			 * Case 2: right-to-left associative operator: The next operator must only contain operators
			 * of higher or the same precedence. Example: Parsed first "=" in "a = b = c*d". Now use the
			 * expression parser for evaluating "b = c*d" as second operand.
			 */
			Associativity associativity = operator.getAssociativity();
			int operatorPrecedenceLevelForNextOperand = operator.getPrecedenceLevel() - (associativity == Associativity.LEFT_TO_RIGHT ? 1 : 0);

			if (considerOperatorResult && stopCircuitEvaluation(accumulatedResultInfo, operator)) {
				parserToolbox = deriveToolboxWithoutEvaluation(parserToolbox);
				considerOperatorResult = false;
			}
			log(LogLevel.INFO, "parsing next operand");
			AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation> nextOperandParser = parserToolbox.createExpressionParser(operatorPrecedenceLevelForNextOperand);
			ObjectParseResult nextOperand = nextOperandParser.parse(tokenStream, contextInfo, operandExpectation);
			ObjectInfo nextOperandInfo = nextOperand.getObjectInfo();
			operands.add(nextOperand);
			try {
				// Check syntax even if result of operator is not considered because of short circuit evaluation
				ObjectInfo operatorResult = applyOperator(parserToolbox.getOperatorResultProvider(), accumulatedResultInfo, nextOperandInfo, operator);
				if (considerOperatorResult) {
					accumulatedResultInfo = operatorResult;
				}
				log(LogLevel.SUCCESS, "applied operator successfully");
			} catch (OperatorException e) {
				log(LogLevel.ERROR, "applying operator failed: " + e.getMessage());
				throw new InternalParseException(e.getMessage());
			} catch (Throwable t) {
				StringBuilder builder = new StringBuilder("applying operator '" + operator + "' failed: ");
				String error = ParseUtils.formatException(t, builder).toString();
				log(LogLevel.ERROR, error);
				throw new InternalParseException(error, t);
			}
		}
	}

	private static ObjectInfo applyOperator(OperatorResultProvider operatorResultProvider, ObjectInfo lhs, ObjectInfo rhs, BinaryOperator operator) throws OperatorException {
		return OPERATOR_IMPLEMENTATIONS.get(operator).apply(operatorResultProvider, lhs, rhs);
	}

	private static boolean stopCircuitEvaluation(ObjectInfo objectInfo, BinaryOperator operator) {
		return operator == BinaryOperator.LOGICAL_AND	&& Boolean.FALSE.equals(objectInfo.getObject())
			|| operator == BinaryOperator.LOGICAL_OR	&& Boolean.TRUE.equals(objectInfo.getObject());
	}

	private ParserToolbox deriveToolboxWithoutEvaluation(ParserToolbox parserToolbox) {
		ParserSettings settings = parserToolbox.getSettings();
		if (!settings.isEnableDynamicTyping()) {
			return parserToolbox;
		}
		ParserSettings settingsWithoutEvaluation = settings.builder().enableDynamicTyping(false).build();
		return new ParserToolbox(parserToolbox.getThisInfo(), settingsWithoutEvaluation);
	}

	@FunctionalInterface
	private interface OperatorImplementation
	{
		ObjectInfo apply(OperatorResultProvider operatorResultProvider, ObjectInfo lhs, ObjectInfo rhs) throws OperatorException;
	}

	private static class ExpressionParseResult extends AbstractObjectParseResult
	{
		private final List<ObjectParseResult>	operands;
		private final List<BinaryOperator>		operators;

		ExpressionParseResult(List<ObjectParseResult> operands, List<BinaryOperator> operators, ObjectInfo expressionInfo, int position) {
			super(expressionInfo, position);

			Preconditions.checkArgument(operands.size() == operators.size() + 1);

			/*
			 * Since only the assignment operator evaluates from right to left and has the highest
			 * precedence level (= lowest precedence), it is guaranteed (except for possible coding
			 * bugs) that this constructor is only called for 1 operator if it is right-to-left
			 * associative.
			 */
			boolean canBeEvaluatedFromLeftToRight = operators.size() == 1
				|| operators.stream().allMatch(op -> op.getAssociativity() == Associativity.LEFT_TO_RIGHT);
			Preconditions.checkArgument(canBeEvaluatedFromLeftToRight);

			this.operands = operands;
			this.operators = operators;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws ParseException {
			// evaluate from left to right
			ObjectInfo accumulatedResultInfo = operands.get(0).evaluate(thisInfo, contextInfo);
			for (int i = 0; i < operators.size(); i++) {
				BinaryOperator operator = operators.get(i);
				if (stopCircuitEvaluation(accumulatedResultInfo, operator)) {
					return accumulatedResultInfo;
				}
				ObjectParseResult nextOperand = operands.get(i + 1);
				ObjectInfo nextOperandInfo = nextOperand.evaluate(thisInfo, contextInfo);
				try {
					accumulatedResultInfo = applyOperator(OPERATOR_RESULT_PROVIDER, accumulatedResultInfo, nextOperandInfo, operator);
				} catch (OperatorException e) {
					throw new ParseException(nextOperand.getPosition(), "Exception when evaluating operator '" + operator + "': " + e.getMessage(), e);
				}
			}
			return accumulatedResultInfo;
		}
	}
}
