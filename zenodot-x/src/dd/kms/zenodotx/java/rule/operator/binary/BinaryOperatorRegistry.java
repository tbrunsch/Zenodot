package dd.kms.zenodotx.java.rule.operator.binary;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodotx.common.NullableOptional;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.overloadresolution.OverloadResolver;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BinaryOperatorRegistry
{
	private final Multimap<String, BinaryOperatorInfo>	operatorInfos	= ArrayListMultimap.create();

	public Collection<String> getOperators() {
		return operatorInfos.keySet();
	}

	public Collection<BinaryOperatorInfo> getOperatorInfos(String operator) {
		return operatorInfos.get(operator);
	}

	public List<BinaryOperatorInfo> getMatchingOperatorInfos(String operator, Class<?> lhsOperandType) {
		OverloadResolver<BinaryOperatorInfo> overloadResolver = new OverloadResolver<>(lhsOperandType);
		Collection<BinaryOperatorInfo> operatorInfos = getOperatorInfos(operator);
		for (BinaryOperatorInfo operatorInfo : operatorInfos) {
			overloadResolver.registerRegularOverload(operatorInfo, operatorInfo.getLhsOperandClass());
		}
		return overloadResolver.getMatchingOverloads();
	}

	public BinaryOperatorInfo getBestMatchingOperatorInfo(String operator, Class<?> lhsOperandType, Class<?> rhsOperandType) throws SemanticException {
		OverloadResolver<BinaryOperatorInfo> overloadResolver = new OverloadResolver<>(lhsOperandType, rhsOperandType);
		Collection<BinaryOperatorInfo> operatorInfos = getOperatorInfos(operator);
		for (BinaryOperatorInfo operatorInfo : operatorInfos) {
			overloadResolver.registerRegularOverload(operatorInfo, operatorInfo.getLhsOperandClass(), operatorInfo.getRhsOperandClass());
		}
		List<BinaryOperatorInfo> bestMatchingOperatorInfos = overloadResolver.getBestMatchingOverloads();
		int size = bestMatchingOperatorInfos.size();
		if (size == 0) {
			String leftTarget = BinaryOperators.createOperandDescription(lhsOperandType);
			String rightTarget = BinaryOperators.createOperandDescription(rhsOperandType);
			throw new SemanticException("Binary operator '" + operator + "' cannot be applied to " + leftTarget + " and " + rightTarget);
		} else if (size > 1) {
			Class<?> lhsOperandClass1 = bestMatchingOperatorInfos.get(0).getLhsOperandClass();
			Class<?> rhsOperandClass1 = bestMatchingOperatorInfos.get(0).getRhsOperandClass();
			Class<?> lhsOperandClass2 = bestMatchingOperatorInfos.get(1).getLhsOperandClass();
			Class<?> rhsOperandClass2 = bestMatchingOperatorInfos.get(1).getRhsOperandClass();
			throw new SemanticException("Binary operator '" + operator + "' is ambiguous for types '" + lhsOperandType.getSimpleName() + "' and '" + rhsOperandType.getSimpleName() + "': Implementations are available for ('" + lhsOperandClass1.getSimpleName() + "', '" + rhsOperandClass1.getSimpleName() + "') and ('" + lhsOperandClass2.getSimpleName() + "', '" + rhsOperandClass2.getSimpleName() + "')");
		} else {
			return bestMatchingOperatorInfos.get(0);
		}
	}

	public <L, R> void register(String operator, Class<L> lhsOperandClass, Class<R> rhsOperandClass, Class<?> resultClass, BiFunction<L, R, ?> operatorImplementation) {
		register(operator, lhsOperandClass, rhsOperandClass, resultClass, BinaryOperatorMode.RETURN_RESULT, operatorImplementation);
	}

	public <L, R> void register(String operator, Class<L> lhsOperandClass, Class<R> rhsOperandClass, Class<?> resultClass, BinaryOperatorMode operatorMode, BiFunction<L, R, ?> operatorImplementation) {
		register(operator, lhsOperandClass, rhsOperandClass, resultClass, operatorMode, operatorImplementation, null);
	}

	public <L, R> void register(String operator, Class<L> lhsOperandClass, Class<R> rhsOperandClass, Class<?> resultClass, BinaryOperatorMode operatorMode, BiFunction<L, R, ?> operatorImplementation, @Nullable Function<L, ? extends NullableOptional<?>> shortCircuitImplementation) {
		register(operator, lhsOperandClass, rhsOperandClass, operatorMode, operatorImplementation, shortCircuitImplementation, (classA, classB) -> resultClass);
	}

	/**
	 * @apiNote The result class is the class of the result of the {@code operatorImplementation}, not necessarily the result of the operator itself.
	 *          Usually, both are the same. However, there is a difference for operators that assign the result to one of the two operands and
	 *          eventually returns the operand. This is the case for the assignment operator "=": The assignment operator implementation is
	 *          {@code (a, b) -> b}, i.e., its result class is the class of {@code b}. However, the whole operator's result class is that of {@code a}.
	 */
	public <L, R> void register(String operator, Class<L> lhsOperandClass, Class<R> rhsOperandClass, BinaryOperatorMode operatorMode, BiFunction<L, R, ?> operatorImplementation, @Nullable Function<L, ? extends NullableOptional<?>> shortCircuitImplementation, BiFunction<Class<? extends L>, Class<? extends R>, ? extends Class<?>> resultClassProvider) {
		BiFunction<Object, Object, Object> wrappedImplementation = (a, b) -> operatorImplementation.apply(ReflectionUtils.convertTo(a, lhsOperandClass, false), ReflectionUtils.convertTo(b, rhsOperandClass, false));
		Function<Object, NullableOptional<?>> wrappedShortCircuitImplementation = shortCircuitImplementation != null
				? a -> shortCircuitImplementation.apply(ReflectionUtils.convertTo(a, lhsOperandClass, false))
				: null;
		BiFunction<Class<?>, Class<?>, Class<?>> wrappedResultClassProvider = (classA, classB) -> resultClassProvider.apply((Class<? extends L>) classA, (Class<? extends R>) classB);
		BinaryOperatorInfo operatorInfo = new BinaryOperatorInfo(operator, lhsOperandClass, rhsOperandClass, wrappedResultClassProvider, operatorMode, wrappedImplementation, wrappedShortCircuitImplementation);
		operatorInfos.put(operator, operatorInfo);
	}
}
