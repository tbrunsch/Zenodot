package dd.kms.zenodotx.java.rule.operator.binary;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodotx.common.NullableOptional;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BinaryOperatorRegistry
{
	private final Multimap<String, BinaryOperatorInfo>	operatorInfos	= ArrayListMultimap.create();

	public <L, R> void register(String operator, Class<L> lhsOperandClass, Class<R> rhsOperandClass, Class<?> resultClass, BiFunction<L, R, ?> operatorImplementation) {
		register(operator, lhsOperandClass, rhsOperandClass, resultClass, BinaryOperatorMode.RETURN_RESULT, operatorImplementation);
	}

	public <L, R> void register(String operator, Class<L> lhsOperandClass, Class<R> rhsOperandClass, Class<?> resultClass, BinaryOperatorMode operatorMode, BiFunction<L, R, ?> operatorImplementation) {
		register(operator, lhsOperandClass, rhsOperandClass, resultClass, operatorMode, operatorImplementation, null);
	}

	public <L, R> void register(String operator, Class<L> lhsOperandClass, Class<R> rhsOperandClass, Class<?> resultClass, BinaryOperatorMode operatorMode, BiFunction<L, R, ?> operatorImplementation, @Nullable Function<L, ? extends NullableOptional<?>> shortCircuitImplementation) {
		BiFunction<Object, Object, Object> wrappedImplementation = (a, b) -> operatorImplementation.apply(ReflectionUtils.convertTo(a, lhsOperandClass, false), ReflectionUtils.convertTo(b, rhsOperandClass, false));
		Function<Object, NullableOptional<?>> wrappedShortCircuitImplementation = shortCircuitImplementation != null
				? a -> shortCircuitImplementation.apply(ReflectionUtils.convertTo(a, lhsOperandClass, false))
				: null;
		BinaryOperatorInfo operatorInfo = new BinaryOperatorInfo(operator, lhsOperandClass, rhsOperandClass, resultClass, operatorMode, wrappedImplementation, wrappedShortCircuitImplementation);
		operatorInfos.put(operator, operatorInfo);
	}
}
