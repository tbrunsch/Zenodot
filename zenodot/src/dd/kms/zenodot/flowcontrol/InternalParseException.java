package dd.kms.zenodot.flowcontrol;

import dd.kms.zenodot.result.ParseError;

public class InternalParseException extends Exception
{
	private final int						position;
	private final ParseError.ErrorPriority	priority;

	public InternalParseException(int position, String message, ParseError.ErrorPriority priority) {
		super(message);
		this.position = position;
		this.priority = priority;
	}

	public int getPosition() {
		return position;
	}

	public ParseError.ErrorPriority getPriority() {
		return priority;
	}
}
