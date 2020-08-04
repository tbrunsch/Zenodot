package dd.kms.zenodot.api.result;

/**
 * Closed-open interval with integer boundaries
 */
public interface IntRange
{
	/**
	 * Range start (inclusive)
	 */
	int getBegin();

	/**
	 * Range end (exclusive)
	 */
	int getEnd();
}
