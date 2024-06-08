package dd.kms.zenodot.api.common;

/**
 * This method is thrown when it is not possible to access the value of a field or to execute
 * a method or constructor due to missing constraints.
 */
public class AccessDeniedException extends ReflectiveOperationException
{
	public AccessDeniedException(String message, Throwable cause) {
		super(message, cause);
	}
}
