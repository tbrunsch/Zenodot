package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;

public class ClassFieldRule extends AbstractFieldRule<Class<?>>
{
	ClassFieldRule() {
		name("Class field");
	}

	@Override
	protected Class<?> getContextType(Class<?> context, JavaSettings settings) {
		return context;
	}

	@Override
	protected InstanceParseResult getContextInstanceEvaluation(Class<?> context, JavaSettings settings) {
		return NullParseResult.RESULT;
	}

	@Override
	protected boolean isContextStatic() {
		return false;
	}
}
