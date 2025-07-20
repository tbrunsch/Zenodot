package dd.kms.zenodotx.java.rule.operator.unary;

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

	private final boolean withAssignment;

	UnaryOperatorMode(boolean withAssignment) {
		this.withAssignment = withAssignment;
	}

	public boolean isWithAssignment() {
		return withAssignment;
	}
}
