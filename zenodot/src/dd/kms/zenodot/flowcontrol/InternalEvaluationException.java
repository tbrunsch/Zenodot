package dd.kms.zenodot.flowcontrol;

public class InternalEvaluationException extends Exception
{
	public InternalEvaluationException(String message) {
		super("Internal error: " + message);
	}

	public InternalEvaluationException(String message, Throwable cause) {
		super(message, cause);
	}
}
