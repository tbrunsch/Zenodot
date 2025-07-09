package dd.kms.zenodotx.java.rule;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.zenodot.api.common.ReflectionUtils;

import java.util.Collection;
import java.util.function.Function;

public class UnaryPrefixOperatorRegistry
{
	private final Multimap<String, UnaryPrefixOperatorInfo>	operatorInfos	= ArrayListMultimap.create();

	public Collection<String> getOperators() {
		return operatorInfos.keySet();
	}

	public Collection<UnaryPrefixOperatorInfo> getOperatorInfos(String operator) {
		return operatorInfos.get(operator);
	}

	public <T> void register(String operator, Class<T> operandClass, Class<?> resultClass, Function<T, ?> operatorImplementation) {
		Function<Object, Object> wrappedImplementation = o -> operatorImplementation.apply(ReflectionUtils.convertTo(o, operandClass, false));
		UnaryPrefixOperatorInfo operatorInfo = new UnaryPrefixOperatorInfo(operandClass, resultClass, wrappedImplementation);
		operatorInfos.put(operator, operatorInfo);
	}
}
