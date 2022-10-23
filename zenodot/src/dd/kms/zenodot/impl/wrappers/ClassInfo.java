package dd.kms.zenodot.impl.wrappers;

/**
 * References a class by its fully qualified (normalized) name. That way, a class does not have to be loaded
 * until it is used.<br/>
 * <br/>
 * See {@link dd.kms.zenodot.impl.utils.ClassUtils#normalizeClassName(String)} for a description what is meant by "normalized".
 */
public interface ClassInfo
{
	String getNormalizedName();

	/**
	 * Yields a class name without further qualification. This is in most cases the same
	 * as the simple class name. However, the name returned by this method may contain
	 * leading numbers (Java technicalities), whereas simple class names do not.
	 */
	String getUnqualifiedName();
}
