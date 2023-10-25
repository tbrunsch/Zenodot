package dd.kms.zenodot.api;

import dd.kms.zenodot.framework.tokenizer.TokenStream;

import java.text.MessageFormat;

public class ParseException extends Exception
{
	private final String	expression;
	private final int		position;

	public ParseException(TokenStream tokenStream, String message) {
		this(tokenStream, message, null);
	}

	public ParseException(TokenStream tokenStream, String message, Throwable cause) {
		this(tokenStream.getExpression(), tokenStream.getPosition(), message, cause);
	}

	public ParseException(String expression, int position, String message, Throwable cause) {
		super(MessageFormat.format("Parse exception at position {0}: {1}", position, message), cause);
		this.expression = expression;
		this.position = position;
	}

	public String getExpression() {
		return expression;
	}

	public int getPosition() {
		return position;
	}
}
