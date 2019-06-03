package dd.kms.zenodot.matching;

/**
 * Different ratings for access match. Only relevant for fields and methods.<br/>
 * <br/>
 * The lower the ordinal, the better the match.
 */
public enum AccessMatch
{
	/**
	 * Used for fields and methods that are accessed as expected, i.e., static fields/methods
	 * via class and non-static fields/methods via instance.<br/>
	 */
	FULL,

	/**
	 * Used for static fields and method that are accessed via an instance instead of a class.
	 */
	STATIC_ACCESS_VIA_INSTANCE,

	/**
	 * Used to rate anything except fields and methods (packages, classes, variables, object
	 * tree nodes) where the distinction between static and non-static does not make sense.
	 */
	IGNORED
}
