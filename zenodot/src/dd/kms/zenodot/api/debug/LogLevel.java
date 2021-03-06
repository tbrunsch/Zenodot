package dd.kms.zenodot.api.debug;

public enum LogLevel
{
	SUCCESS("Success: "),
	ERROR("Error: "),
	WARNING("Warning: "),
	INFO("");

	private final String prefix;

	LogLevel(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}
}
