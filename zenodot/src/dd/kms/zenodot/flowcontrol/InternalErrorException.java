package dd.kms.zenodot.flowcontrol;

public class InternalErrorException extends Exception
{
	public InternalErrorException(String message) {
		super(message);
	}

	public InternalErrorException(String message, Throwable cause) {
		super(message, cause);
	}
}
