package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.AbstractCombineRule;

public class UnaryPrefixOperatorExecuteRule extends AbstractCombineRule<String, InstanceParseResult, InstanceParseResult, JavaSettings>
{
	private final UnaryOperatorRegistry registry;

	public UnaryPrefixOperatorExecuteRule(UnaryOperatorRegistry registry, Rule<Void, InstanceParseResult, JavaSettings> simpleExpression) {
		super(simpleExpression);
		this.registry = registry;
	}

	@Override
	protected InstanceParseResult combine(String unaryPrefixOperator, InstanceParseResult operandParseResult, JavaSettings settings) throws SemanticException {
		EvaluationMode evaluationMode = settings.getEvaluationMode();
		ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(evaluationMode);
		ObjectInfo instanceInfo = operandParseResult.getEvaluatedResult();
		Class<?> instanceType = objectInfoProvider.getType(instanceInfo);
		UnaryOperatorInfo operatorInfo = registry.getBestMatchingOperatorInfo(unaryPrefixOperator, instanceType);
		try {
			return new UnaryOperatorParseResult(operatorInfo, operandParseResult, settings.getEvaluationMode());
		} catch (EvaluationException e) {
			throw new SemanticException(e.getMessage());
		}
	}
}
