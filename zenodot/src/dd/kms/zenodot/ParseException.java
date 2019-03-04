package dd.kms.zenodot;

import java.text.MessageFormat;

public class ParseException extends Exception
{
	ParseException(int position, String message) {
		this(position, message, null);
	}

	ParseException(int position, String message, Throwable cause) {
		super(MessageFormat.format("Parse exception at position {0}: {1}", position, message), cause);
	}
}
