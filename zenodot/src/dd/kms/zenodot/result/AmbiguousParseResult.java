package dd.kms.zenodot.result;

/**
 * An instance of this interface is returned when an ambiguity has been encountered during
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
public interface AmbiguousParseResult extends ParseOutcome
{
	String getMessage();
}
