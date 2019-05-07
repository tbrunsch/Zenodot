package dd.kms.zenodot.settings;

class VariableImpl implements Variable
{
	private final String	name;
	private final Object	value;
	private final boolean	useHardReference;

	public VariableImpl(String name, Object value, boolean useHardReference) {
		this.name = name;
		this.value = value;
		this.useHardReference = useHardReference;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getValue() {
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
