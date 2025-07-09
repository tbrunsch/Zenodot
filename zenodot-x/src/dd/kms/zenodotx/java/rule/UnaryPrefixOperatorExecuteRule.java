package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.matching.MatchRatings;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.AbstractCombineRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class UnaryPrefixOperatorExecuteRule extends AbstractCombineRule<String, InstanceParseResult, InstanceParseResult, JavaSettings>
{
	private final UnaryPrefixOperatorRegistry	registry;

	public UnaryPrefixOperatorExecuteRule(UnaryPrefixOperatorRegistry registry, Rule<Void, InstanceParseResult, JavaSettings> simpleExpression) {
		super(simpleExpression);
		this.registry = registry;
	}

	@Override
	protected InstanceParseResult combine(String unaryPrefixOperator, InstanceParseResult instanceParseResult, JavaSettings settings) throws SemanticException, EvaluationException {
		EvaluationMode evaluationMode = settings.getEvaluationMode();
		ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
		ObjectInfo instanceInfo = instanceParseResult.getEvaluatedResult();
		Class<?> instanceType = objectInfoProvider.getType(instanceInfo);
		UnaryPrefixOperatorInfo operatorInfo = getBestMatchingOperatorInfo(unaryPrefixOperator, instanceType);
		return new UnaryPrefixOperatorParseResult(operatorInfo, instanceParseResult, settings.getEvaluationMode());
	}

	private UnaryPrefixOperatorInfo getBestMatchingOperatorInfo(String unaryPrefixOperator, Class<?> instanceType) throws SemanticException {
		Collection<UnaryPrefixOperatorInfo> operatorInfos = registry.getOperatorInfos(unaryPrefixOperator);

		TypeMatch bestTypeMatch = TypeMatch.NONE;
		List<UnaryPrefixOperatorInfo> bestMatchingOperatorInfos = new ArrayList<>();
		for (UnaryPrefixOperatorInfo operatorInfo : operatorInfos) {
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
			throw new SemanticException("Unary prefix operator '" + unaryPrefixOperator + "' cannot be applied to instances of type '" + instanceType.getSimpleName() + "'");
		}
		if (bestMatchingOperatorInfos.size() > 1) {
			Class<?> operandClass1 = bestMatchingOperatorInfos.get(0).getOperandClass();
			Class<?> operandClass2 = bestMatchingOperatorInfos.get(1).getOperandClass();
			throw new SemanticException("Unary prefix operator '" + unaryPrefixOperator + "' is ambiguous for type '" + instanceType.getSimpleName() + "': Implementations are available for '" + operandClass1 + "' and '" + operandClass2 + "'");
		}
		if (bestMatchingOperatorInfos.isEmpty()) {
			throw new IllegalStateException("Internal error: No best unary operator implementation found though there should be");
		}
		return bestMatchingOperatorInfos.get(0);
	}

	private static class UnaryPrefixOperatorParseResult implements InstanceParseResult
	{
		private final UnaryPrefixOperatorInfo	operatorInfo;
		private final InstanceParseResult		instanceParseResult;
		private final ObjectInfo				evaluatedResult;

		public UnaryPrefixOperatorParseResult(UnaryPrefixOperatorInfo operatorInfo, InstanceParseResult instanceParseResult, EvaluationMode evaluationMode) {
			this.instanceParseResult = instanceParseResult;
			this.operatorInfo = operatorInfo;
			this.evaluatedResult = evaluate(instanceParseResult.getEvaluatedResult(), evaluationMode);
		}

		@Override
		public ObjectInfo getEvaluatedResult() {
			return evaluatedResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) throws EvaluationException {
			ObjectInfo instanceInfo = instanceParseResult.evaluate(thisInfo, variables, evaluationMode);
			return evaluate(instanceInfo, evaluationMode);
		}

		private ObjectInfo evaluate(ObjectInfo instanceInfo, EvaluationMode evaluationMode) {
			Function<Object, Object> operatorImplementation = operatorInfo.getImplementation();
			Object instance = instanceInfo.getObject();
			// TODO: Consider operators with additional assignment
			Object operatorResult = evaluationMode != EvaluationMode.STATIC_TYPING && instance != InfoProvider.INDETERMINATE_VALUE
					? operatorImplementation.apply(instance)
					: InfoProvider.INDETERMINATE_VALUE;
			Class<?> operatorResultClass = operatorInfo.getResultClass();
			return InfoProvider.createObjectInfo(operatorResult, operatorResultClass);
		}
	}
}
