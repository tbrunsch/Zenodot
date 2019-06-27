package dd.kms.zenodot;

import java.text.MessageFormat;

public class ParseException extends Exception
{
	private final int position;

	public ParseException(int position, String message) {
		this(position, message, null);
	}

	public ParseException(int position, String message, Throwable cause) {
		super(MessageFormat.format("Parse exception at position {0}: {1}", position, message), cause);
		this.position = position;
	}

	public int getPosition() {
		return position;
	}
}
