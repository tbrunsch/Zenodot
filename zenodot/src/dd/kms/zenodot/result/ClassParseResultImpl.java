package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.TypeInfo;

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
