package dd.kms.zenodot.matching;

class MatchRatingImpl implements MatchRating
{
	private final StringMatch	nameMatch;
	private final TypeMatch		typeMatch;
	private final AccessMatch	accessMatch;

	MatchRatingImpl(StringMatch nameMatch, TypeMatch typeMatch, AccessMatch accessMatch) {
		this.nameMatch = nameMatch;
		this.typeMatch = typeMatch;
		this.accessMatch = accessMatch;
	}

	@Override
	public int compareTo(MatchRating that) {
		int nameMatchComparisonResult = nameMatch.compareTo(that.getNameMatch());
		if (nameMatchComparisonResult != 0) {
			return nameMatchComparisonResult;
		}
		int typeMatchComparisonResult = typeMatch.compareTo(that.getTypeMatch());
		if (typeMatchComparisonResult != 0) {
			return typeMatchComparisonResult;
		}
		if (accessMatch == AccessMatch.IGNORED || that.getAccessMatch() == AccessMatch.IGNORED) {
			// do not compare access match if it is not relevant
			return 0;
		}
		int accessMatchComparisonResult = accessMatch.compareTo(that.getAccessMatch());
		return accessMatchComparisonResult;
	}

	@Override
	public StringMatch getNameMatch() {
		return nameMatch;
	}

	@Override
	public TypeMatch getTypeMatch() {
		return typeMatch;
	}

	@Override
	public AccessMatch getAccessMatch() {
		return accessMatch;
	}

	@Override
	public String toString() {
		return "name match: " + nameMatch + ", type match: " + typeMatch + ", access match: " + accessMatch;
	}
}
