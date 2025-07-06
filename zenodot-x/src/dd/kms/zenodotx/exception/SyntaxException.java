package dd.kms.zenodotx.exception;

public class SyntaxException extends Exception
{
	private int parsePosition	= -1;

	public SyntaxException(String message) {
		super(message);
	}

	public SyntaxException(String message, int parsePosition) {
		super(message);
		setParsePosition(parsePosition);
	}

	public int getParsePosition() {
		return parsePosition;
	}

	public void setParsePosition(int parsePosition) {
		this.parsePosition = parsePosition;
	}
}
