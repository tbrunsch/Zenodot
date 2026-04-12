package dd.kms.zenodotx.java.rule.operator.binary;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.overloadresolution.OverloadResolver;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
			overloadResolver.registerRegularOverload(operatorInfo, operatorInfo.getLhsOperandType());
		}
		return overloadResolver.getMatchingOverloads();
	}

	public BinaryOperatorInfo getBestMatchingOperatorInfo(String operator, Class<?> lhsOperandType, Class<?> rhsOperandType) throws SemanticException {
		OverloadResolver<BinaryOperatorInfo> overloadResolver = new OverloadResolver<>(lhsOperandType, rhsOperandType);
		Collection<BinaryOperatorInfo> operatorInfos = getOperatorInfos(operator);
		for (BinaryOperatorInfo operatorInfo : operatorInfos) {
			overloadResolver.registerRegularOverload(operatorInfo, operatorInfo.getLhsOperandType(), operatorInfo.getRhsOperandType());
		}
		List<BinaryOperatorInfo> bestMatchingOperatorInfos = overloadResolver.getBestMatchingOverloads().stream()
			.filter(info -> info.isApplicableToOperandTypes(lhsOperandType, rhsOperandType))
			.collect(Collectors.toList());
		int size = bestMatchingOperatorInfos.size();
		if (size == 0) {
			String leftTarget = BinaryOperators.createOperandDescription(lhsOperandType);
			String rightTarget = BinaryOperators.createOperandDescription(rhsOperandType);
			throw new SemanticException("Binary operator '" + operator + "' cannot be applied to " + leftTarget + " and " + rightTarget);
		} else if (size > 1) {
			Class<?> lhsOperandType1 = bestMatchingOperatorInfos.get(0).getLhsOperandType();
			Class<?> rhsOperandType1 = bestMatchingOperatorInfos.get(0).getRhsOperandType();
			Class<?> lhsOperandType2 = bestMatchingOperatorInfos.get(1).getLhsOperandType();
			Class<?> rhsOperandType2 = bestMatchingOperatorInfos.get(1).getRhsOperandType();
			throw new SemanticException("Binary operator '" + operator + "' is ambiguous for types '" + lhsOperandType.getSimpleName() + "' and '" + rhsOperandType.getSimpleName() + "': Implementations are available for ('" + lhsOperandType1.getSimpleName() + "', '" + rhsOperandType1.getSimpleName() + "') and ('" + lhsOperandType2.getSimpleName() + "', '" + rhsOperandType2.getSimpleName() + "')");
		} else {
			return bestMatchingOperatorInfos.get(0);
		}
	}

	public void register(BinaryOperatorInfo operatorInfo) {
		operatorInfos.put(operatorInfo.getOperator(), operatorInfo);
	}
}
