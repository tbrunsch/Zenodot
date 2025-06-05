package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;

public class MethodNameOfThisRule extends AbstractMethodNameRule<Void>
{
	MethodNameOfThisRule() {
		name("this.Method name");
	}

	@Override
	protected Class<?> getContextType(Void context, JavaSettings settings) {
		ObjectInfo thisInfo = settings.getThisInfo();
		ObjectInfoProvider objectInfoProvider = new ObjectInfoProvider(settings.getEvaluationMode());
		return objectInfoProvider.getType(thisInfo);
	}

	@Override
	protected InstanceParseResult getContextInstanceEvaluation(Void context, JavaSettings settings) {
		return new ThisParseResult(settings.getThisInfo());
	}

	@Override
	protected boolean isContextStatic() {
		return false;
	}
}
