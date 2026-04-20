package dd.kms.zenodotx.java.rule.operator.binary;

import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodotx.common.NullableOptional;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class BinaryOperatorInfoBuilder<L, R, RESULT>
{
	private final String				operator;
	private final Class<L>				lhsOperandType;
	private final Class<R>				rhsOperandType;
	private final BiFunction<Class<? extends L>, Class<? extends R>, ? extends Class<?>>	resultTypeProvider;
	private final BiFunction<L, R, ?>	operatorImplementation;

	private BinaryOperatorMode	operatorMode	= BinaryOperatorMode.RETURN_RESULT;

	@Nullable
	private Function<L, ? extends NullableOptional<?>>	shortCircuitImplementation	= null;

	@Nullable
	private BiPredicate<? super Class<? extends L>, ? super Class<? extends R>>	applicableToOperandTypesPredicate		= null;

	public BinaryOperatorInfoBuilder(String operator, Class<L> lhsOperandType, Class<R> rhsOperandType, Class<RESULT> resultType, BiFunction<L, R, ?> operatorImplementation) {
		this(operator, lhsOperandType, rhsOperandType, (typeLhs, typeRhs) -> resultType, operatorImplementation);
	}

	/**
	 * @param resultTypeProvider: The result type is the type of the result of the {@code operatorImplementation} and
	 *                            not necessarily of the result of the operator itself. Usually, both are the same.
	 *                            However, there is a difference for operators that assign the result to one of the two
	 *                            operands and eventually return the operand. This is the case for the assignment
	 *                            operator "=": The assignment operator implementation is {@code (a, b) -> b}, i.e., its
	 *                            result type is the type of {@code b}. However, the whole operator's result type is
	 *                            that of {@code a}.
	 */
	public BinaryOperatorInfoBuilder(String operator, Class<L> lhsOperandType, Class<R> rhsOperandType, BiFunction<Class<? extends L>, Class<? extends R>, ? extends Class<?>> resultTypeProvider, BiFunction<L, R, ?> operatorImplementation) {
		this.operator = operator;
		this.lhsOperandType = lhsOperandType;
		this.rhsOperandType = rhsOperandType;
		this.operatorImplementation = operatorImplementation;
		this.resultTypeProvider = resultTypeProvider;
	}

	public BinaryOperatorInfoBuilder<L, R, RESULT> operatorMode(BinaryOperatorMode operatorMode) {
		this.operatorMode = operatorMode;
		return this;
	}

	public BinaryOperatorInfoBuilder<L, R, RESULT> shortCircuitImplementation(Function<L, ? extends NullableOptional<?>> shortCircuitImplementation) {
		this.shortCircuitImplementation = shortCircuitImplementation;
		return this;
	}

	public BinaryOperatorInfoBuilder<L, R, RESULT> restrictApplicability(BiPredicate<? super Class<? extends L>, ? super Class<? extends R>> applicableToOperandTypesPredicate) {
		this.applicableToOperandTypesPredicate = applicableToOperandTypesPredicate;
		return this;
	}

	public BinaryOperatorInfo build() {
		BiFunction<Object, Object, Object> wrappedImplementation = (a, b) -> operatorImplementation.apply(ReflectionUtils.convertTo(a, lhsOperandType, false), ReflectionUtils.convertTo(b, rhsOperandType, false));
		Function<Object, NullableOptional<?>> wrappedShortCircuitImplementation = shortCircuitImplementation != null
			? a -> shortCircuitImplementation.apply(ReflectionUtils.convertTo(a, lhsOperandType, false))
			: null;
		BiFunction<Class<?>, Class<?>, Class<?>> wrappedResultTypeProvider = (typeLhs, typeRhs) -> resultTypeProvider.apply((Class<? extends L>) typeLhs, (Class<? extends R>) typeRhs);
		BiPredicate<Class<?>, Class<?>> wrappedApplicableToOperandTypesPredicate = applicableToOperandTypesPredicate != null
			? (typeLhs, typeRhs) -> applicableToOperandTypesPredicate.test((Class<? extends L>) typeLhs, (Class<? extends R>) typeRhs)
			: null;
		return new BinaryOperatorInfo(operator, lhsOperandType, rhsOperandType, wrappedApplicableToOperandTypesPredicate, wrappedResultTypeProvider, operatorMode, wrappedImplementation, wrappedShortCircuitImplementation);
	}
}
