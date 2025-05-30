package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;

public class InstanceMethodNameRule extends AbstractMethodNameRule<InstanceParseResult>
{
	InstanceMethodNameRule() {
		name("Instance method name");
	}

	@Override
	protected Class<?> getContextType(InstanceParseResult context, JavaSettings settings) {
		ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(settings.getEvaluationMode());
		ObjectInfo evaluatedResult = context.getEvaluatedResult();
		return objectInfoProvider.getType(evaluatedResult);
	}

	@Override
	protected InstanceParseResult getContextInstanceEvaluation(InstanceParseResult context, JavaSettings settings) {
		return context;
	}

	@Override
	protected boolean isContextStatic() {
		return false;
	}
}
