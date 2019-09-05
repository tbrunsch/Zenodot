package dd.kms.zenodot.settings;

import dd.kms.zenodot.utils.wrappers.ObjectInfo;

class VariableImpl implements Variable
{
	private final String		name;
	private final ObjectInfo	value;
	private final boolean		useHardReference;

	public VariableImpl(String name, ObjectInfo value, boolean useHardReference) {
		this.name = name;
		this.value = value;
		this.useHardReference = useHardReference;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ObjectInfo getValue() {
		return value;
	}

	@Override
	public boolean isUseHardReference() {
		return useHardReference;
	}

	@Override
	public String toString() {
		return name + ": " + (value == null ? "NULL" : value.toString());
	}
}
