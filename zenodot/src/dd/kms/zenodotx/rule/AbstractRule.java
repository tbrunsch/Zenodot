package dd.kms.zenodotx.rule;

public abstract class AbstractRule<I, O, S> implements Rule<I, O, S>
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

	@Override
	public String toString() {
		return name != null ? name : super.toString();
	}
}
