package dd.kms.zenodot.matching;

/**
 * Rating of a match of a completion suggestions. Takes ratings for the name and the
 * type match into account. Priority is on the name match.
 */
public class MatchRating implements Comparable<MatchRating>
{
	public static final MatchRating	NONE	= new MatchRating(StringMatch.NONE, TypeMatch.NONE, AccessMatch.IGNORED);

	private final StringMatch	nameMatch;
	private final TypeMatch		typeMatch;
	private final AccessMatch	accessMatch;

	public MatchRating(StringMatch nameMatch, TypeMatch typeMatch, AccessMatch accessMatch) {
		this.nameMatch = nameMatch;
		this.typeMatch = typeMatch;
		this.accessMatch = accessMatch;
	}

	@Override
	public int compareTo(MatchRating that) {
		int nameMatchComparisonResult = nameMatch.compareTo(that.nameMatch);
		if (nameMatchComparisonResult != 0) {
			return nameMatchComparisonResult;
		}
		int typeMatchComparisonResult = typeMatch.compareTo(that.typeMatch);
		if (typeMatchComparisonResult != 0) {
			return typeMatchComparisonResult;
		}
		int accessMatchComparisonResult = accessMatch.compareTo(that.accessMatch);
		return accessMatchComparisonResult;
	}

	public StringMatch getNameMatch() {
		return nameMatch;
	}

	public TypeMatch getTypeMatch() {
		return typeMatch;
	}

	public AccessMatch getAccessMatch() {
		return accessMatch;
	}

	@Override
	public String toString() {
		return "name match: " + nameMatch + ", type match: " + typeMatch + ", access match: " + accessMatch;
	}
}
