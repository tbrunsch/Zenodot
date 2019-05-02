package dd.kms.zenodot.matching;

/**
 * Rating of a match of a completion suggestions. Takes ratings for the name and the
 * type match into account. Priority is on the name match.
 */
public class MatchRating implements Comparable<MatchRating>
{
	public static final MatchRating	NONE	= new MatchRating(StringMatch.NONE, TypeMatch.NONE);

	private final StringMatch	nameMatch;
	private final TypeMatch		typeMatch;

	public MatchRating(StringMatch nameMatch, TypeMatch typeMatch) {
		this.nameMatch = nameMatch;
		this.typeMatch = typeMatch;
	}

	@Override
	public int compareTo(MatchRating that) {
		int nameMatchComparisonResult = nameMatch.compareTo(that.nameMatch);
		return nameMatchComparisonResult != 0
				? nameMatchComparisonResult
				: typeMatch.compareTo(that.typeMatch);
	}

	public StringMatch getNameMatch() {
		return nameMatch;
	}

	public TypeMatch getTypeMatch() {
		return typeMatch;
	}

	@Override
	public String toString() {
		return "name match: " + nameMatch + ", type match: " + typeMatch;
	}
}
