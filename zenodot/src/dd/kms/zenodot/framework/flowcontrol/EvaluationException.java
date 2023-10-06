package dd.kms.zenodot.framework.flowcontrol;

public class EvaluationException extends Exception
{
	public EvaluationException(String message) {
		super("Internal error: " + message);
	}

	public EvaluationException(String message, Throwable cause) {
		super(message, cause);
	}
}
