package dd.kms.zenodot.tokenizer;

public class Token
{
	private final String	value;
	private final boolean   containsCaret;

	Token(String value, boolean containsCaret) {
		this.value = value;
		this.containsCaret = containsCaret;
	}

	public String getValue() {
		return value;
	}

	public boolean isContainsCaret() {
		return containsCaret;
	}

	@Override
	public String toString() {
		return value;
	}
}