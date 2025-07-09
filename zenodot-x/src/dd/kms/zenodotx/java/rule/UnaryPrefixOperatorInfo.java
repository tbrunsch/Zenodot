package dd.kms.zenodotx.java.rule;

import java.util.function.Function;

public class UnaryPrefixOperatorInfo {
	private final Class<?> operandClass;
	private final Class<?> resultClass;
	private final Function<Object, Object> implementation;

	UnaryPrefixOperatorInfo(Class<?> operandClass, Class<?> resultClass, Function<Object, Object> implementation) {
		this.operandClass = operandClass;
		this.resultClass = resultClass;
		this.implementation = implementation;
	}

	public Class<?> getOperandClass() {
		return operandClass;
	}

	public Class<?> getResultClass() {
		return resultClass;
	}

	public Function<Object, Object> getImplementation() {
		return implementation;
	}
}
