package dd.kms.zenodotx.java.rule.operator.binary;

import dd.kms.zenodotx.common.NullableOptional;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class BinaryOperatorInfo
{
	private final String									operator;
	private final Class<?>									lhsOperandType;
	private final Class<?>									rhsOperandType;

	/**
	 * If {@code null}, then there is no additional restriction regarding the applicability to given operand types.
	 */
	@Nullable
	private final BiPredicate<Class<?>, Class<?>>			applicableToOperandTypesPredicate;
	private final BiFunction<Class<?>, Class<?>, Class<?>>	resultTypeProvider;
	private final BinaryOperatorMode						operatorMode;
	private final BiFunction<Object, Object, Object>		implementation;

	/**
	 * If {@code null}, then short circuit evaluation is not possible.
	 */
	@Nullable
	private final Function<Object, NullableOptional<?>>		shortCircuitImplementation;

	/**
	 * Use the {@link BinaryOperatorInfoBuilder} to create instances of this class.
	 */
	BinaryOperatorInfo(String operator, Class<?> lhsOperandType, Class<?> rhsOperandType, @Nullable BiPredicate<Class<?>, Class<?>> applicableToOperandTypesPredicate, BiFunction<Class<?>, Class<?>, Class<?>> resultTypeProvider, BinaryOperatorMode operatorMode, BiFunction<Object, Object, Object> implementation, @Nullable Function<Object, NullableOptional<?>> shortCircuitImplementation) {
		if (shortCircuitImplementation != null && operatorMode == BinaryOperatorMode.RETURN_RIGHT_OPERAND_ASSIGN_RESULT_RIGHT) {
			throw new IllegalArgumentException("Binary operators that support short circuit evaluation must not return the right operand");
		}
		this.operator = operator;
		this.lhsOperandType = lhsOperandType;
		this.rhsOperandType = rhsOperandType;
		this.applicableToOperandTypesPredicate = applicableToOperandTypesPredicate;
		this.resultTypeProvider = resultTypeProvider;
		this.operatorMode = operatorMode;
		this.implementation = implementation;
		this.shortCircuitImplementation = shortCircuitImplementation;
	}

	public String getOperator() {
		return operator;
	}

	public Class<?> getLhsOperandType() {
		return lhsOperandType;
	}

	public Class<?> getRhsOperandType() {
		return rhsOperandType;
	}

	public boolean isApplicableToOperandTypes(Class<?> actualLhsOperandType, Class<?> actualRhsOperandType) {
		return applicableToOperandTypesPredicate == null
			|| applicableToOperandTypesPredicate.test(actualLhsOperandType, actualRhsOperandType);
	}

	public BiFunction<Class<?>, Class<?>, Class<?>> getResultTypeProvider() {
		return resultTypeProvider;
	}

	public BinaryOperatorMode getOperatorMode() {
		return operatorMode;
	}

	public BiFunction<Object, Object, Object> getImplementation() {
		return implementation;
	}

	@Nullable
	public Function<Object, NullableOptional<?>> getShortCircuitImplementation() {
		return shortCircuitImplementation;
	}
}
