package dd.kms.zenodotx.rule;

import dd.kms.zenodotx.GrammarSettings;

public abstract class AbstractRule<I, O, S extends GrammarSettings> implements Rule<I, O, S>
{
	private String	name;

	@Override
	public Rule<I, O, S> name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String name() {
		return name;
	}

	protected String getGenericName() {
		return null;
	}

	@Override
	public String toString() {
		if (name != null) {
			return name;
		}
		String genericName = getGenericName();
		if (genericName != null) {
			return genericName;
		}
		return super.toString();
	}
}
