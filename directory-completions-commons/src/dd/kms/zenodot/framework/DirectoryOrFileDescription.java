package dd.kms.zenodot.framework;

public abstract class DirectoryOrFileDescription
{
	private final String name;

	DirectoryOrFileDescription(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
