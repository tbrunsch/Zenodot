package dd.kms.zenodot.impl.result;

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
