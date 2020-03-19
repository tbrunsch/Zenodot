package dd.kms.zenodot.result;

public enum ParseResultType
{
	OBJECT	("an object"),
	CLASS	("a class"),
	PACKAGE	("a package");

	private final String description;

	ParseResultType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
