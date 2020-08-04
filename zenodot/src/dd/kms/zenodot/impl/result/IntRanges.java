package dd.kms.zenodot.impl.result;

import dd.kms.zenodot.api.result.IntRange;

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
