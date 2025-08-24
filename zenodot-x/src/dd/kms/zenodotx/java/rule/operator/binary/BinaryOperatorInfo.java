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
	private final BiFunction<Class<?>, Class<?>, Class<?>>				resultClassProvider;
	private final BinaryOperatorMode									operatorMode;
	private final BiFunction<Object, Object, Object>					implementation;
	@Nullable
	private final Function<Object, NullableOptional<? extends Object>>	shortCircuitImplementation;

	public BinaryOperatorInfo(String operator, Class<?> lhsOperandClass, Class<?> rhsOperandClass, BiFunction<Class<?>, Class<?>, Class<?>> resultClassProvider, BinaryOperatorMode operatorMode, BiFunction<Object, Object, Object> implementation, @Nullable Function<Object, NullableOptional<? extends Object>> shortCircuitImplementation) {
		if (shortCircuitImplementation != null && operatorMode == BinaryOperatorMode.RETURN_RIGHT_OPERAND_ASSIGN_RESULT_RIGHT) {
			throw new IllegalArgumentException("Binary operators that support short circuit evaluation must not return the right operand");
		}
		this.operator = operator;
		this.lhsOperandClass = lhsOperandClass;
		this.rhsOperandClass = rhsOperandClass;
		this.resultClassProvider = resultClassProvider;
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

	public BiFunction<Class<?>, Class<?>, Class<?>> getResultClassProvider() {
		return resultClassProvider;
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
