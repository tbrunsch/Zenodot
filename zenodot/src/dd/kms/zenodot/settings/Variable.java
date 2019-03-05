package dd.kms.zenodot.settings;

public class Variable
{
	private final String	name;
	private final Object	value;
	private final boolean	useHardReference;

	public Variable(String name, Object value, boolean useHardReference) {
		this.name = name;
		this.value = value;
		this.useHardReference = useHardReference;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public boolean isUseHardReference() {
		return useHardReference;
	}

	@Override
	public String toString() {
		return name + ": " + (value == null ? "NULL" : value.toString());
	}
}
