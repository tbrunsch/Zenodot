package dd.kms.zenodot.impl.result;

import dd.kms.zenodot.api.result.ClassParseResult;
import dd.kms.zenodot.api.wrappers.TypeInfo;

class ClassParseResultImpl implements ClassParseResult
{
	private final TypeInfo type;

	ClassParseResultImpl(TypeInfo type) {
		this.type = type;
	}

	@Override
	public TypeInfo getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.toString();
	}
}
