package dd.kms.zenodotx.java.rule.operator.unary;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.common.Pair;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.EvaluationRule;

public class UnaryPrefixOperatorExecuteRule extends AbstractRule<Pair<String, InstanceParseResult>, InstanceParseResult, JavaSettings> implements EvaluationRule<Pair<String, InstanceParseResult>, InstanceParseResult, JavaSettings>
{
	private final UnaryOperatorRegistry registry;

	public UnaryPrefixOperatorExecuteRule(UnaryOperatorRegistry registry) {
		this.registry = registry;
	}

	@Override
	public InstanceParseResult evaluate(Pair<String, InstanceParseResult> pair, JavaSettings settings) throws SemanticException, EvaluationException {
		String unaryPrefixOperator = pair.getFirst();
		InstanceParseResult operandParseResult = pair.getSecond();
		EvaluationMode evaluationMode = settings.getEvaluationMode();
		ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
		ObjectInfo operandInfo = operandParseResult.getEvaluatedResult();
		Class<?> operandType = objectInfoProvider.getType(operandInfo);
		UnaryOperatorInfo operatorInfo = registry.getBestMatchingOperatorInfo(unaryPrefixOperator, operandType);
		try {
			return new UnaryOperatorParseResult(operatorInfo, operandParseResult, settings.getEvaluationMode());
		} catch (EvaluationException e) {
			throw new SemanticException(e.getMessage());
		}
	}
}
