package dd.kms.zenodot.result;

/**
 * An instance of this class is returned when an ambiguity has been encountered during
 * the parsing process. This can be the case when
 * <ul>
 *     <li>
 *         method overloads cannot be uniquely resolved or
 *     </li>
 *     <li>
 *         when the expression can be evaluated to both, a variable and a field of {@code this}.
 *     </li>
 * </ul>
 */
public class AmbiguousParseResult implements ParseResult
{
	private final int		position;
	private final String	message;

	public AmbiguousParseResult(int position, String message) {
		this.position = position;
		this.message = message;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.AMBIGUOUS_PARSE_RESULT;
	}

	@Override
	public int getPosition() {
		return position;
	}

	public String getMessage() {
		return message;
	}
}
