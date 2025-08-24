package dd.kms.zenodotx.java.rule.operator.binary;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.matching.MatchRatings;
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
			String operator = operatorInfo.getOperator();
			BinaryOperatorMode operatorMode = operatorInfo.getOperatorMode();

			String targetOperandSide = null;
			ObjectInfo targetOperandInfo = null;
			if (operatorMode.isWithAssignmentLeft()) {
				targetOperandSide = "left";
				targetOperandInfo = lhsOperandInfo;
			} else if (operatorMode.isWithAssignmentRight()) {
				targetOperandSide = "right";
				targetOperandInfo = rhsOperandInfo;
			}

			ObjectInfo.ValueSetter targetOperandSetter = null;
			if (targetOperandInfo != null) {
				targetOperandSetter = targetOperandInfo.getValueSetter();
				if (targetOperandSetter == null) {
					throw new EvaluationException("Operator \"" + operator + "\" cannot be applied because the " + targetOperandSide + " operand does not permit assignments");
				}
			}

			Object lhsOperand = lhsOperandInfo.getObject();
			Object rhsOperand = rhsOperandInfo.getObject();

			Function<Object, NullableOptional<?>> shortCircuitImplementation = operatorInfo.getShortCircuitImplementation();
			NullableOptional<?> shortCircuitValue = shortCircuitImplementation != null && lhsOperand != InfoProvider.INDETERMINATE_VALUE
				? shortCircuitImplementation.apply(lhsOperand)
				: NullableOptional.empty();
			Object operatorResult;
			if (shortCircuitValue.isPresent()) {
				operatorResult = shortCircuitValue.get();
			} else {
				BiFunction<Object, Object, Object> operatorImplementation = operatorInfo.getImplementation();
				try {
					operatorResult = evaluationMode != EvaluationMode.STATIC_TYPING && lhsOperand != InfoProvider.INDETERMINATE_VALUE && rhsOperand != InfoProvider.INDETERMINATE_VALUE
						? operatorImplementation.apply(lhsOperand, rhsOperand)
						: InfoProvider.INDETERMINATE_VALUE;
				} catch (RuntimeException e) {
					throw new EvaluationException("Error executing operator \"" + operator + "\": " + e.getMessage(), e);
				}
			}

			BiFunction<Class<?>, Class<?>, Class<?>> resultClassProvider = operatorInfo.getResultClassProvider();

			Class<?> lhsOperandClass = lhsOperandInfo.getDeclaredType();
			Class<?> rhsOperandClass = rhsOperandInfo.getDeclaredType();

			/*
			 * The result class of the operator implementation, not of the whole operator.
			 * For the assignment operator this is a difference:
			 *   - result class: class of RHS
			 *   - operator's result class: class of LHS
			 */
			Class<?> resultClass = resultClassProvider.apply(lhsOperandClass, rhsOperandClass);

			final ObjectInfo resultInfo;
			switch (operatorMode) {
				case RETURN_RESULT: {
					resultInfo = InfoProvider.createObjectInfo(operatorResult, resultClass);
					break;
				}
				case RETURN_RESULT_ASSIGN_RESULT_LEFT: {
					// e.g. assignment operator: result class = RHS class, but operator result class = LHS class
					if (operatorResult != InfoProvider.INDETERMINATE_VALUE && operatorResult != null) {
						try {
							operatorResult = ReflectionUtils.convertTo(operatorResult, lhsOperandClass, true);
						} catch (ClassCastException e) {
							throw new EvaluationException("Instance of type '" + operatorResult.getClass().getName() + "' cannot be assigned to declared type '" + lhsOperandClass.getName() + "'");
						}
					}
					resultInfo = InfoProvider.createObjectInfo(operatorResult, lhsOperandClass);
					break;
				}
				case RETURN_RESULT_ASSIGN_RESULT_RIGHT: {
					// analogy to assignment operator: operator result class = RHS class
					if (operatorResult != InfoProvider.INDETERMINATE_VALUE && operatorResult != null) {
						try {
							operatorResult = ReflectionUtils.convertTo(operatorResult, rhsOperandClass, true);
						} catch (ClassCastException e) {
							throw new EvaluationException("Instance of type '" + operatorResult.getClass().getName() + "' cannot be assigned to declared type '" + rhsOperandClass.getName() + "'");
						}
					}
					resultInfo = InfoProvider.createObjectInfo(operatorResult, rhsOperandClass);
					break;
				}
				case RETURN_LEFT_OPERAND_ASSIGN_RESULT_LEFT: {
					resultInfo = InfoProvider.createObjectInfo(lhsOperand, lhsOperandClass);
					break;
				}
				case RETURN_RIGHT_OPERAND_ASSIGN_RESULT_RIGHT: {
					resultInfo = InfoProvider.createObjectInfo(rhsOperand, rhsOperandClass);
					break;
				}
				default:
					throw new IllegalStateException("Unsupported operator mode: " + operatorMode);
			}

			if (targetOperandInfo != null) {
				ObjectInfo assignInfo = InfoProvider.createObjectInfo(operatorResult, resultClass, targetOperandSetter);
				ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
				Class<?> sourceType = objectInfoProvider.getType(assignInfo);

				Class<?> declaredTargetType = targetOperandInfo.getDeclaredType();
				TypeMatch typeMatch = MatchRatings.rateTypeMatch(declaredTargetType, sourceType);
				if (typeMatch == TypeMatch.NONE) {
					throw new EvaluationException("Instance of type '" + sourceType.getName() + "' cannot be assigned to declared type '" + declaredTargetType.getName() + "'");
				}

				if (evaluationMode == EvaluationMode.DYNAMIC_TYPING) {
					try {
						targetOperandSetter.setObjectInfo(assignInfo);
					} catch (IllegalArgumentException e) {
						throw new EvaluationException("Assignment failed: " + e.getMessage(), e);
					}
				}
			}

			return resultInfo;
		}
	}
}
