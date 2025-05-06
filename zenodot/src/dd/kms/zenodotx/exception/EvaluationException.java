package dd.kms.zenodotx.exception;

public class EvaluationException extends Exception
{
	public EvaluationException() {}

	public EvaluationException(String message) {
		super(message);
	}

	public EvaluationException(String message, Throwable cause) {
		super(message, cause);
	}
}
