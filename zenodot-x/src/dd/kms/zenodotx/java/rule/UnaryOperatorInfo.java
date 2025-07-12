package dd.kms.zenodotx.java.rule;

import java.util.function.Function;

public class UnaryOperatorInfo
{
	private final String					operator;
	private final Class<?>					operandClass;
	private final Class<?>					resultClass;
	private final UnaryOperatorMode			operatorMode;
	private final Function<Object, Object>	implementation;

	UnaryOperatorInfo(String operator, Class<?> operandClass, Class<?> resultClass, UnaryOperatorMode operatorMode, Function<Object, Object> implementation) {
		this.operator = operator;
		if (operatorMode.isWithAssignment() && !operandClass.isAssignableFrom(resultClass)) {
			throw new IllegalStateException("Invalid operator definition: Since the result of the operator \"" + operator + "\" is assigned to the operand, the result class must be assignable to the operand class.");
		}
		this.operandClass = operandClass;
		this.resultClass = resultClass;
		this.operatorMode = operatorMode;
		this.implementation = implementation;
	}

	public String getOperator() {
		return operator;
	}

	public Class<?> getOperandClass() {
		return operandClass;
	}

	public Class<?> getResultClass() {
		return resultClass;
	}

	public UnaryOperatorMode getOperatorMode() {
		return operatorMode;
	}

	public Function<Object, Object> getImplementation() {
		return implementation;
	}

	public enum UnaryOperatorMode
	{
		/**
		 * The operator is executed and the result is returned. There is no side effect. Example: {@code !b}
		 */
		RETURN_RESULT(false),

		/**
		 * The operator is executed. The result is returned and assigned. Example: {@code ++i}
		 */
		RETURN_RESULT_ASSIGN_RESULT(true),

		/**
		 * The operand is returned. The operator is executed and the result is assigned. Example: {@code i++}
		 */
		RETURN_OPERAND_ASSIGN_RESULT(true);

		private final boolean	withAssignment;

		UnaryOperatorMode(boolean withAssignment) {
			this.withAssignment = withAssignment;
		}

		public boolean isWithAssignment() {
			return withAssignment;
		}
	}
}
