package dd.kms.zenodot.result;

public class IntRanges
{
	/**
	 * @param begin	inclusive
	 * @param end	exclusive
	 */
	public static IntRange create(int begin, int end) {
		return new IntRangeImpl(begin, end);
	}
}
