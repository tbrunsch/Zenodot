package dd.kms.zenodotx.java.rule;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.framework.matching.MatchRatings;
import dd.kms.zenodotx.exception.SemanticException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class UnaryOperatorRegistry
{
	private final Multimap<String, UnaryOperatorInfo>	operatorInfos	= ArrayListMultimap.create();

	public Collection<String> getOperators() {
		return operatorInfos.keySet();
	}

	public Collection<UnaryOperatorInfo> getOperatorInfos(String operator) {
		return operatorInfos.get(operator);
	}

	public UnaryOperatorInfo getBestMatchingOperatorInfo(String operator, Class<?> instanceType) throws SemanticException {
		Collection<UnaryOperatorInfo> operatorInfos = getOperatorInfos(operator);

		TypeMatch bestTypeMatch = TypeMatch.NONE;
		List<UnaryOperatorInfo> bestMatchingOperatorInfos = new ArrayList<>();
		for (UnaryOperatorInfo operatorInfo : operatorInfos) {
			Class<?> operandClass = operatorInfo.getOperandClass();
			TypeMatch typeMatch = MatchRatings.rateTypeMatch(instanceType, operandClass);
			int comparisonResult = typeMatch.compareTo(bestTypeMatch);
			if (comparisonResult < 0) {
				bestMatchingOperatorInfos.clear();
				bestTypeMatch = typeMatch;
			}
			if (comparisonResult <= 0) {
				bestMatchingOperatorInfos.add(operatorInfo);
			}
		}
		if (bestTypeMatch == TypeMatch.NONE) {
			throw new SemanticException("Unary operator '" + operator + "' cannot be applied to instances of type '" + instanceType.getSimpleName() + "'");
		}
		if (bestMatchingOperatorInfos.size() > 1) {
			Class<?> operandClass1 = bestMatchingOperatorInfos.get(0).getOperandClass();
			Class<?> operandClass2 = bestMatchingOperatorInfos.get(1).getOperandClass();
			throw new SemanticException("Unary operator '" + operator + "' is ambiguous for type '" + instanceType.getSimpleName() + "': Implementations are available for '" + operandClass1 + "' and '" + operandClass2 + "'");
		}
		if (bestMatchingOperatorInfos.isEmpty()) {
			throw new IllegalStateException("Internal error: No best unary operator implementation found though there should be");
		}
		return bestMatchingOperatorInfos.get(0);
	}

	public <T> void register(String operator, Class<T> operandClass, Class<?> resultClass, Function<T, ?> operatorImplementation) {
		register(operator, operandClass, resultClass, UnaryOperatorInfo.UnaryOperatorMode.RETURN_RESULT, operatorImplementation);
	}

	public <T> void register(String operator, Class<T> operandClass, Class<?> resultClass, UnaryOperatorInfo.UnaryOperatorMode operatorMode, Function<T, ?> operatorImplementation) {
		Function<Object, Object> wrappedImplementation = o -> operatorImplementation.apply(ReflectionUtils.convertTo(o, operandClass, false));
		UnaryOperatorInfo operatorInfo = new UnaryOperatorInfo(operator, operandClass, resultClass, operatorMode, wrappedImplementation);
		operatorInfos.put(operator, operatorInfo);
	}
}
