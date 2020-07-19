package dd.kms.zenodot.flowcontrol;

public class InternalParseException extends Exception
{
	public InternalParseException(String message) {
		super(message);
	}

	public InternalParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
