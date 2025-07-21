package dd.kms.zenodotx.java.rule.operator.binary;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.common.NullableOptional;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.CompoundRule;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BinaryOperatorExecuteRule extends AbstractRule<InstanceParseResult, InstanceParseResult, JavaSettings> implements CompoundRule<InstanceParseResult, InstanceParseResult, JavaSettings>
{
	private final BinaryOperatorRegistry						registry;
	private final Rule<Void, String, JavaSettings>				operatorRule;
	private final Rule<Void, InstanceParseResult, JavaSettings>	rightHandSideRule;

	public BinaryOperatorExecuteRule(BinaryOperatorRegistry registry, Rule<Void, InstanceParseResult, JavaSettings> rightHandSideRule) {
		this.registry = registry;
		this.operatorRule = new BinaryOperatorParseRule(registry);
		this.rightHandSideRule = rightHandSideRule;
	}

	@Override
	public InstanceParseResult parse(InstanceParseResult lhs, JavaSettings settings, Parser<JavaSettings> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		String operator = parser.parse(operatorRule, null, settings);
		EvaluationMode evaluationMode = settings.getEvaluationMode();
		ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
		ObjectInfo lhsOperandInfo = lhs.getEvaluatedResult();
		Class<?> lhsOperandType = objectInfoProvider.getType(lhsOperandInfo);
		List<BinaryOperatorInfo> operatorInfos = registry.getBestMatchingOperatorInfos(operator, lhsOperandType);
		Object lhsOperand = lhsOperandInfo.getObject();
		boolean shortCircuitEvaluation = isApplyShortCircuitEvaluation(lhsOperand, operatorInfos);
		JavaSettings rhsSettings = getRightHandSideSettings(settings, shortCircuitEvaluation);
		InstanceParseResult rhs = parser.parse(rightHandSideRule, null, rhsSettings);
		ObjectInfo rhsOperandInfo = rhs.getEvaluatedResult();
		Class<?> rhsOperandType = objectInfoProvider.getType(rhsOperandInfo);
		BinaryOperatorInfo operatorInfo = registry.getBestMatchingOperatorInfo(operator, lhsOperandType, rhsOperandType);
		try {
			return new BinaryOperatorInstanceParseResult(lhs, rhs, operatorInfo, settings.getEvaluationMode());
		} catch (EvaluationException e) {
			throw new SemanticException(e.getMessage());
		}
	}

	@Override
	public void parseSyntactically(Parser<JavaSettings> parser, JavaSettings settings) throws SyntaxException {
		parser.parseSyntactically(operatorRule, settings);
		parser.parseSyntactically(rightHandSideRule, settings);
	}

	private static boolean isApplyShortCircuitEvaluation(Object lhsOperand, List<BinaryOperatorInfo> operatorInfos) throws SemanticException {
		Boolean totalApplyShortCircuitEvaluation = null;
		for (BinaryOperatorInfo operatorInfo : operatorInfos) {
			Function<Object, NullableOptional<?>> shortCircuitImplementation = operatorInfo.getShortCircuitImplementation();
			boolean applyShortCircuitEvaluation = isApplyShortCircuitEvaluation(lhsOperand, operatorInfo);
			if (totalApplyShortCircuitEvaluation != null && applyShortCircuitEvaluation != totalApplyShortCircuitEvaluation) {
				throw new SemanticException("Different implementations of operator '" + operatorInfo.getOperator() + "' dictate a different behavior regarding short circuit evaluation");
			}
			totalApplyShortCircuitEvaluation = applyShortCircuitEvaluation;
		}
		return Boolean.TRUE.equals(totalApplyShortCircuitEvaluation);
	}

	private static boolean isApplyShortCircuitEvaluation(Object lhsOperand, BinaryOperatorInfo operatorInfo) {
		if (lhsOperand == InfoProvider.INDETERMINATE_VALUE) {
			return false;
		}
		Function<Object, NullableOptional<?>> shortCircuitImplementation = operatorInfo.getShortCircuitImplementation();
		if (shortCircuitImplementation == null) {
			return false;
		}
		NullableOptional<?> shortCircuitResultValue = shortCircuitImplementation.apply(lhsOperand);
		return shortCircuitResultValue.isPresent();
	}

	private static JavaSettings getRightHandSideSettings(JavaSettings settings, boolean shortCircuitEvaluation) {
		return shortCircuitEvaluation
				? settings.withoutEvaluation()
				: settings;
	}

	@Override
	protected String getGenericName() {
		return "evaluate ... op rhs";
	}

	private static class BinaryOperatorInstanceParseResult implements InstanceParseResult
	{
		private final InstanceParseResult	lhs;
		private final InstanceParseResult	rhs;
		private final BinaryOperatorInfo	operatorInfo;
		private final ObjectInfo			evaluatedResult;

		private BinaryOperatorInstanceParseResult(InstanceParseResult lhs, InstanceParseResult rhs, BinaryOperatorInfo operatorInfo, EvaluationMode evaluationMode) throws EvaluationException {
			this.lhs = lhs;
			this.rhs = rhs;
			this.operatorInfo = operatorInfo;
			this.evaluatedResult = evaluate(lhs.getEvaluatedResult(), rhs.getEvaluatedResult(), evaluationMode);
		}

		@Override
		public ObjectInfo getEvaluatedResult() {
			return evaluatedResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) throws EvaluationException {
			ObjectInfo lhsOperandInfo = lhs.evaluate(thisInfo, variables, evaluationMode);
			boolean shortCircuitEvaluation = isApplyShortCircuitEvaluation(lhsOperandInfo.getObject(), operatorInfo);
			EvaluationMode rhsEvaluationMode = shortCircuitEvaluation ? EvaluationMode.STATIC_TYPING : EvaluationMode.DYNAMIC_TYPING;
			ObjectInfo rhsOperandInfo = rhs.evaluate(thisInfo, variables, rhsEvaluationMode);
			return evaluate(lhsOperandInfo, rhsOperandInfo, evaluationMode);
		}

		private ObjectInfo evaluate(ObjectInfo lhsOperandInfo, ObjectInfo rhsOperandInfo, EvaluationMode evaluationMode) throws EvaluationException {
			ObjectInfo.ValueSetter lhsOperandSetter = lhsOperandInfo.getValueSetter();
			ObjectInfo.ValueSetter rhsOperandSetter = rhsOperandInfo.getValueSetter();
			BinaryOperatorMode operatorMode = operatorInfo.getOperatorMode();

			if (operatorMode.isWithAssignmentLeft() && lhsOperandSetter == null) {
				String operator = operatorInfo.getOperator();
				throw new EvaluationException("Operator \"" + operator + "\" cannot be applied because the left operand does not permit assignments");
			}
			if (operatorMode.isWithAssignmentRight() && rhsOperandSetter == null) {
				String operator = operatorInfo.getOperator();
				throw new EvaluationException("Operator \"" + operator + "\" cannot be applied because the right operand does not permit assignments");
			}

			Object lhsOperand = lhsOperandInfo.getObject();
			Object rhsOperand = rhsOperandInfo.getObject();

			Function<Object, NullableOptional<?>> shortCircuitImplementation = operatorInfo.getShortCircuitImplementation();
			NullableOptional<?> shortCircuitValue = shortCircuitImplementation != null && lhsOperand != InfoProvider.INDETERMINATE_VALUE
				? shortCircuitImplementation.apply(lhsOperand)
				: NullableOptional.empty();
			final Object operatorResult;
			if (shortCircuitValue.isPresent()) {
				operatorResult = shortCircuitValue.get();
			} else {
				BiFunction<Object, Object, Object> operatorImplementation = operatorInfo.getImplementation();
				operatorResult = evaluationMode != EvaluationMode.STATIC_TYPING && lhsOperand != InfoProvider.INDETERMINATE_VALUE && rhsOperand != InfoProvider.INDETERMINATE_VALUE
					? operatorImplementation.apply(lhsOperand, rhsOperand)
					: InfoProvider.INDETERMINATE_VALUE;
			}

			Class<?> operatorResultClass = operatorInfo.getResultClass();

			final ObjectInfo resultInfo;
			switch (operatorMode) {
				case RETURN_RESULT:
				case RETURN_RESULT_ASSIGN_RESULT_LEFT:
				case RETURN_RESULT_ASSIGN_RESULT_RIGHT: {
					resultInfo = InfoProvider.createObjectInfo(operatorResult, operatorResultClass);
					break;
				}
				case RETURN_LEFT_OPERAND_ASSIGN_RESULT_LEFT: {
					resultInfo = InfoProvider.createObjectInfo(lhsOperand, lhsOperandInfo.getDeclaredType());
					break;
				}
				case RETURN_RIGHT_OPERAND_ASSIGN_RESULT_RIGHT: {
					resultInfo = InfoProvider.createObjectInfo(rhsOperand, rhsOperandInfo.getDeclaredType());
					break;
				}
				default:
					throw new IllegalStateException("Unsupported operator mode: " + operatorMode);
			}

			if (operatorMode.isWithAssignmentLeft()) {
				// TODO: Check that type of value to assign is assignable to target type
				if (evaluationMode == EvaluationMode.DYNAMIC_TYPING) {
					ObjectInfo assignInfo = InfoProvider.createObjectInfo(operatorResult, operatorResultClass, lhsOperandSetter);
					lhsOperandSetter.setObjectInfo(assignInfo);
				}
			}

			if (operatorMode.isWithAssignmentRight()) {
				// TODO: Check that type of value to assign is assignable to target type
				if (evaluationMode == EvaluationMode.DYNAMIC_TYPING) {
					ObjectInfo assignInfo = InfoProvider.createObjectInfo(operatorResult, operatorResultClass, rhsOperandSetter);
					rhsOperandSetter.setObjectInfo(assignInfo);
				}
			}

			return resultInfo;
		}
	}
}
