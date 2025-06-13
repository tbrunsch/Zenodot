package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;

public class InstanceFieldRule extends AbstractFieldRule<InstanceParseResult>
{
	InstanceFieldRule() {
		name("Instance field");
	}

	@Override
	protected Class<?> getContextType(InstanceParseResult context, JavaSettings settings) throws EvaluationException {
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

	@Override
	protected String getGenericName() {
		return "instance.field";
	}
}
