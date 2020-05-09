package dd.kms.zenodot.matching;

/**
 * Rating of a match of a code completion. Takes ratings for the name and the
 * type match into account. Priority is on the name match.<br/>
 * <br/>
 * A new instance can be created via {@link MatchRatings#create(StringMatch, TypeMatch, AccessMatch)}.
 */
public interface MatchRating extends Comparable<MatchRating>
{
	StringMatch getNameMatch();
	TypeMatch getTypeMatch();
	AccessMatch getAccessMatch();
}
