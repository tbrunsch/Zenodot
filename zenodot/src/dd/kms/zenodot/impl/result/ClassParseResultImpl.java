package dd.kms.zenodot.impl.result;

import dd.kms.zenodot.api.result.ClassParseResult;

class ClassParseResultImpl implements ClassParseResult
{
	private final Class<?> type;

	ClassParseResultImpl(Class<?> type) {
		this.type = type;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public String toString() {
		return type.toString();
	}
}
