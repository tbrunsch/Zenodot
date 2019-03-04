package dd.kms.zenodot.settings;

/**
 * Describes a value that can be referenced in an expression by its name.<br/>
 * <br/>
 * You can specify whether the value should be referenced with a hard or a weak reference
 * by the {@link ParserSettings}. If you decide for a weak reference, then the framework
 * does not prolong the life time of the variable's value to allow for garbage collection.
 */
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
