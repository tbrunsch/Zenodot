package dd.kms.zenodotx.java.rule.operator.binary;

public enum BinaryOperatorMode
{
	/**
	 * The operator is executed and the result is returned. There is no side effect. Example: {@code a + b}
	 */
	RETURN_RESULT(false, false),

	/**
	 * The operator is executed. The result is returned and assigned to the left-hand side. Example: {@code a += b}
	 */
	RETURN_RESULT_ASSIGN_RESULT_LEFT(true, false),

	/**
	 * The operator is executed. The result is returned and assigned to the right-hand side.
	 */
	RETURN_RESULT_ASSIGN_RESULT_RIGHT(false, true),

	/**
	 * The left operand is returned. The operator is executed and the result is assigned to the left operand.
	 */
	RETURN_LEFT_OPERAND_ASSIGN_RESULT_LEFT(true, false),

	/**
	 * The right operand is returned. The operator is executed and the result is assigned to the right operand.
	 */
	RETURN_RIGHT_OPERAND_ASSIGN_RESULT_RIGHT(false, true);

	private final boolean withAssignmentLeft;
	private final boolean withAssignmentRight;

	BinaryOperatorMode(boolean withAssignmentLeft, boolean withAssignmentRight) {
		this.withAssignmentLeft = withAssignmentLeft;
		this.withAssignmentRight = withAssignmentRight;
	}

	public boolean isWithAssignmentLeft() {
		return withAssignmentLeft;
	}

	public boolean isWithAssignmentRight() {
		return withAssignmentRight;
	}
}
