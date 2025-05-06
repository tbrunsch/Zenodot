package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;

class NullParseResult implements InstanceParseResult
{
	static final InstanceParseResult	RESULT	= new NullParseResult();

	@Override
	public ObjectInfo getEvaluatedResult() {
		return InfoProvider.NULL_LITERAL;
	}

	@Override
	public ObjectInfo evaluate(JavaSettings settings) {
		return InfoProvider.NULL_LITERAL;
	}
}
