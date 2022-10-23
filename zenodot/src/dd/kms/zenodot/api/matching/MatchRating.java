package dd.kms.zenodot.api.matching;

/**
 * Rating of a match of a code completion. Takes ratings for the name and the
 * type match into account. Priority is on the name match.<br>
 * <br>
 * A rating {@code r1} has higher priority than a rating {@code r2} if {@code r1.compareTo(r2) < 0}.
 */
public interface MatchRating extends Comparable<MatchRating>
{
	StringMatch getNameMatch();
	TypeMatch getTypeMatch();
	boolean isAccessDiscouraged();

	/**
	 * Returns an int representing the priority of that rating. High values represent high priorities.
	 */
	int getRatingValue();
}
