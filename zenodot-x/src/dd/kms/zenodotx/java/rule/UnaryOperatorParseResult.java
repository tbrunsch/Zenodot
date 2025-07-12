package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.java.result.InstanceParseResult;

import java.util.function.Function;

public class UnaryOperatorParseResult implements InstanceParseResult {
	private final UnaryOperatorInfo operatorInfo;
	private final InstanceParseResult operandParseResult;
	private final ObjectInfo evaluatedResult;

	public UnaryOperatorParseResult(UnaryOperatorInfo operatorInfo, InstanceParseResult operandParseResult, EvaluationMode evaluationMode) throws EvaluationException {
		this.operandParseResult = operandParseResult;
		this.operatorInfo = operatorInfo;
		this.evaluatedResult = evaluate(operandParseResult.getEvaluatedResult(), evaluationMode);
	}

	@Override
	public ObjectInfo getEvaluatedResult() {
		return evaluatedResult;
	}

	@Override
	public ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) throws EvaluationException {
		ObjectInfo operandInfo = operandParseResult.evaluate(thisInfo, variables, evaluationMode);
		return evaluate(operandInfo, evaluationMode);
	}

	private ObjectInfo evaluate(ObjectInfo operandInfo, EvaluationMode evaluationMode) throws EvaluationException {
		ObjectInfo.ValueSetter operandSetter = operandInfo.getValueSetter();
		UnaryOperatorInfo.UnaryOperatorMode operatorMode = operatorInfo.getOperatorMode();
		Function<Object, Object> operatorImplementation = operatorInfo.getImplementation();
		Object operand = operandInfo.getObject();
		Object operatorResult = evaluationMode != EvaluationMode.STATIC_TYPING && operand != InfoProvider.INDETERMINATE_VALUE
			? operatorImplementation.apply(operand)
			: InfoProvider.INDETERMINATE_VALUE;
		Class<?> operatorResultClass = operatorInfo.getResultClass();

		final ObjectInfo resultInfo;
		switch (operatorMode) {
			case RETURN_RESULT:
			case RETURN_RESULT_ASSIGN_RESULT: {
				resultInfo = InfoProvider.createObjectInfo(operatorResult, operatorResultClass);
				break;
			}
			case RETURN_OPERAND_ASSIGN_RESULT: {
				resultInfo = InfoProvider.createObjectInfo(operand, operandInfo.getDeclaredType());
				break;
			}
			default:
				throw new IllegalStateException("Unsupported operator mode: " + operatorMode);
		}

		if (operatorMode.isWithAssignment() && evaluationMode == EvaluationMode.DYNAMIC_TYPING) {
			if (operandSetter == null) {
				String operator = operatorInfo.getOperator();
				throw new EvaluationException("Operator \"" + operator + "\" cannot be applied because the operand does not permit assignments");
			}
			ObjectInfo assignInfo = InfoProvider.createObjectInfo(operatorResult, operatorResultClass, operandSetter);
			operandSetter.setObjectInfo(assignInfo);
		}

		return resultInfo;
	}
}
