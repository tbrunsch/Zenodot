package dd.kms.zenodotx.java.rule.operator.binary;

import dd.kms.zenodotx.common.NullableOptional;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BinaryOperatorInfo
{
	private final String												operator;
	private final Class<?>												lhsOperandClass;
	private final Class<?>												rhsOperandClass;
	private final Class<?>												resultClass;
	private final BinaryOperatorMode									operatorMode;
	private final BiFunction<Object, Object, Object>					implementation;
	@Nullable
	private final Function<Object, NullableOptional<? extends Object>>	shortCircuitImplementation;

	public BinaryOperatorInfo(String operator, Class<?> lhsOperandClass, Class<?> rhsOperandClass, Class<?> resultClass, BinaryOperatorMode operatorMode, BiFunction<Object, Object, Object> implementation, Function<Object, NullableOptional<? extends Object>> shortCircuitImplementation) {
		this.operator = operator;
		this.lhsOperandClass = lhsOperandClass;
		this.rhsOperandClass = rhsOperandClass;
		this.resultClass = resultClass;
		this.operatorMode = operatorMode;
		this.implementation = implementation;
		this.shortCircuitImplementation = shortCircuitImplementation;
	}

	public String getOperator() {
		return operator;
	}

	public Class<?> getLhsOperandClass() {
		return lhsOperandClass;
	}

	public Class<?> getRhsOperandClass() {
		return rhsOperandClass;
	}

	public Class<?> getResultClass() {
		return resultClass;
	}

	public BinaryOperatorMode getOperatorMode() {
		return operatorMode;
	}

	public BiFunction<Object, Object, Object> getImplementation() {
		return implementation;
	}

	@Nullable
	public Function<Object, NullableOptional<? extends Object>> getShortCircuitImplementation() {
		return shortCircuitImplementation;
	}
}
