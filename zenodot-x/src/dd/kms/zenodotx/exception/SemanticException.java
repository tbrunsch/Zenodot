package dd.kms.zenodotx.exception;

public class SemanticException extends Exception
{
	private int	parsePosition			= -1;
	private int	syntacticParsePosition	= -1;

	public SemanticException(String message) {
		super(message);
	}

	/**
	 * @return the position at which the exception occurred
	 */
	public int getParsePosition() {
		return parsePosition;
	}

	public void setParsePosition(int parsePosition) {
		this.parsePosition = parsePosition;
	}

	/**
	 * @return the position until which the expression could be parsed syntactically when ignoring
	 *         this semantic exception.
	 */
	public int getSyntacticParsePosition() {
		return syntacticParsePosition;
	}

	public void setSyntacticParsePosition(int syntacticParsePosition) {
		this.syntacticParsePosition = syntacticParsePosition;
	}
}
