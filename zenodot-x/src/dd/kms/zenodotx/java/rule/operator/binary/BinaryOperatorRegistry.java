package dd.kms.zenodotx.java.rule.operator.binary;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.framework.matching.MatchRatings;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodotx.common.NullableOptional;
import dd.kms.zenodotx.exception.SemanticException;

import javax.annotation.Nullable;
import java.util.ArrayList;
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

	public List<BinaryOperatorInfo> getBestMatchingOperatorInfos(String operator, Class<?> lhsOperandType) throws SemanticException {
		Collection<BinaryOperatorInfo> operatorInfos = getOperatorInfos(operator);

		TypeMatch bestTypeMatch = TypeMatch.NONE;
		List<BinaryOperatorInfo> bestMatchingOperatorInfos = new ArrayList<>();
		for (BinaryOperatorInfo operatorInfo : operatorInfos) {
			Class<?> lhsOperandClass = operatorInfo.getLhsOperandClass();
			TypeMatch typeMatch = MatchRatings.rateTypeMatch(lhsOperandClass, lhsOperandType);
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
			if (lhsOperandType == InfoProvider.NO_TYPE) {
				throw new SemanticException("Binary operator '" + operator + "' cannot be applied to null");
			} else {
				throw new SemanticException("Binary operator '" + operator + "' cannot be applied to instances of type '" + lhsOperandType.getSimpleName() + "'");
			}
		}
		if (bestMatchingOperatorInfos.isEmpty()) {
			throw new IllegalStateException("Internal error: No best binary operator implementation found though there should be");
		}
		return bestMatchingOperatorInfos;
	}

	public BinaryOperatorInfo getBestMatchingOperatorInfo(String operator, Class<?> lhsOperandType, Class<?> rhsOperandType) throws SemanticException {
		Collection<BinaryOperatorInfo> operatorInfos = getOperatorInfos(operator);

		TypeMatch bestTypeMatch = TypeMatch.NONE;
		List<BinaryOperatorInfo> bestMatchingOperatorInfos = new ArrayList<>();
		for (BinaryOperatorInfo operatorInfo : operatorInfos) {
			Class<?> lhsOperandClass = operatorInfo.getLhsOperandClass();
			Class<?> rhsOperandClass = operatorInfo.getRhsOperandClass();
			TypeMatch lhsTypeMatch = MatchRatings.rateTypeMatch(lhsOperandClass, lhsOperandType);
			TypeMatch rhsTypeMatch = MatchRatings.rateTypeMatch(rhsOperandClass, rhsOperandType);
			int lhsRhsComparisonResult = lhsTypeMatch.compareTo(rhsTypeMatch);
			TypeMatch typeMatch = lhsRhsComparisonResult >= 0 ? lhsTypeMatch : rhsTypeMatch;	// take the worst of both
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
			if (lhsOperandType == InfoProvider.NO_TYPE || rhsOperandType == InfoProvider.NO_TYPE) {
				throw new SemanticException("Binary operator '" + operator + "' cannot be applied to null");
			} else {
				throw new SemanticException("Binary operator '" + operator + "' cannot be applied to instances of type '" + lhsOperandType.getSimpleName() + "' and '" + rhsOperandType.getSimpleName() + "'");
			}
		}
		if (bestMatchingOperatorInfos.size() > 1) {
			Class<?> lhsOperandClass1 = bestMatchingOperatorInfos.get(0).getLhsOperandClass();
			Class<?> rhsOperandClass1 = bestMatchingOperatorInfos.get(0).getRhsOperandClass();
			Class<?> lhsOperandClass2 = bestMatchingOperatorInfos.get(1).getLhsOperandClass();
			Class<?> rhsOperandClass2 = bestMatchingOperatorInfos.get(1).getRhsOperandClass();
			throw new SemanticException("Binary operator '" + operator + "' is ambiguous for types '" + lhsOperandType.getSimpleName() + "' and '" + rhsOperandType.getSimpleName() + "': Implementations are available for ('" + lhsOperandClass1.getSimpleName() + "', '" + rhsOperandClass1.getSimpleName() + "') and ('" + lhsOperandClass2.getSimpleName() + "', '" + rhsOperandClass2.getSimpleName() + "')");
		}
		if (bestMatchingOperatorInfos.isEmpty()) {
			throw new IllegalStateException("Internal error: No best binary operator implementation found though there should be");
		}
		return bestMatchingOperatorInfos.get(0);
	}

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
