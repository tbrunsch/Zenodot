package dd.kms.zenodot.result;

class IntRangeImpl implements IntRange
{
	private final int begin;
	private final int   end;

	/**
	 * @param begin	inclusive
	 * @param end	exclusive
	 */
	IntRangeImpl(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}

	@Override
	public int getBegin() {
		return begin;
	}

	@Override
	public int getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "[" + begin + ", " + end + ")";
	}
}
