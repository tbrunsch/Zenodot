package dd.kms.zenodot.impl.tokenizer;

public enum UnaryOperator
{
	INCREMENT	("++"),	// prefix increment
	DECREMENT	("--"),	// prefix decrement
	PLUS		("+"),
	MINUS		("-"),
	LOGICAL_NOT	("!"),
	BITWISE_NOT	("~");

	public static UnaryOperator getValue(String operatorString) {
		for (UnaryOperator operator : values()) {
			if (operator.getOperator().equals(operatorString)) {
				return operator;
			}
		}
		return null;
	}

	private final String operator;

	UnaryOperator(String operator) {
		this.operator = operator;
	}

	public String getOperator() {
		return operator;
	}
}
