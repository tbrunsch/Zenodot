package dd.kms.zenodotx.exception;

public class SemanticException extends Exception
{
	private int	parsePosition	= -1;

	public SemanticException(String message) {
		super(message);
	}

	public int getParsePosition() {
		return parsePosition;
	}

	public void setParsePosition(int parsePosition) {
		this.parsePosition = parsePosition;
	}
}
