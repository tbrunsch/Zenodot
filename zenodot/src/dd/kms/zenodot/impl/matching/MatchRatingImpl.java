package dd.kms.zenodot.impl.matching;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.matching.TypeMatch;

import java.util.Objects;

public class MatchRatingImpl implements MatchRating
{
	private final StringMatch nameMatch;
	private final TypeMatch typeMatch;

	/**
	 * This flag is set to true when accessing something in an unwanted way.
	 * Currently, this flag is only set to true when accessing a static field
	 * or method via an object instead of a class.
	 */
	private final boolean		accessDiscouraged;

	public MatchRatingImpl(StringMatch nameMatch, TypeMatch typeMatch, boolean accessDiscouraged) {
		this.nameMatch = nameMatch;
		this.typeMatch = typeMatch;
		this.accessDiscouraged = accessDiscouraged;
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
		if (accessDiscouraged == that.isAccessDiscouraged()) {
			return 0;
		}
		return accessDiscouraged ? 1 : -1;
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
	public boolean isAccessDiscouraged() {
		return accessDiscouraged;
	}

	@Override
	public int getRatingValue() {
		int numStringMatchRatingClasses = StringMatch.values().length;
		int numTypeMatchRatingClasses = TypeMatch.values().length;
		int nameRating = numStringMatchRatingClasses - nameMatch.ordinal() - 1;
		int typeRating = numTypeMatchRatingClasses - typeMatch.ordinal() - 1;
		int accessRating = accessDiscouraged ? 0 : 1;
		return 2*(numTypeMatchRatingClasses*nameRating + typeRating) + accessRating;
	}

	@Override
	public String toString() {
		return "name match: " + nameMatch + ", type match: " + typeMatch + ", access discouraged: " + accessDiscouraged;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MatchRatingImpl that = (MatchRatingImpl) o;
		return nameMatch == that.nameMatch &&
			typeMatch == that.typeMatch &&
			accessDiscouraged == that.accessDiscouraged;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nameMatch, typeMatch, accessDiscouraged);
	}
}
