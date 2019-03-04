package dd.kms.zenodot.result;

/**
 * Closed-open interval with integer boundaries
 */
public class IntRange
{
	/**
	 * inclusive
	 */
	private final int begin;

	/**
	 * exclusive
	 */
	private final int   end;

	public IntRange(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "[" + begin + ", " + end + ")";
	}
}
