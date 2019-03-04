package dd.kms.zenodot.utils.wrappers;

import dd.kms.zenodot.utils.ClassUtils;

import java.util.Objects;

/**
 * References a class by its fully qualified (normalized) name. That way, a class does not have to be loaded
 * until it is used.<br/>
 * <br/>
 * See {@link ClassUtils#normalizeClassName(String)} for a description what is meant by "normalized".
 */
public class ClassInfo
{
	public static ClassInfo forName(String qualifiedClassName) throws ClassNotFoundException {
		return new ClassInfo(ClassUtils.normalizeClassName(qualifiedClassName));
	}

	public static ClassInfo forNameUnchecked(String qualifiedClassName) {
		return new ClassInfo(qualifiedClassName);
	}

	private final String normalizedClassName;

	private ClassInfo(String normalizedClassName) {
		this.normalizedClassName = normalizedClassName;
	}

	public String getNormalizedName() {
		return normalizedClassName;
	}

	/**
	 * Yields a class name without further qualification. This is in most cases the same
	 * as the simple class name. However, the name returned by this method may contain
	 * leading numbers (Java technicalities), whereas simple class names do not.
	 */
	public String getUnqualifiedName() {
		return ClassUtils.getLeafOfPath(normalizedClassName);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassInfo that = (ClassInfo) o;
		return Objects.equals(normalizedClassName, that.normalizedClassName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(normalizedClassName);
	}

	@Override
	public String toString() {
		return normalizedClassName;
	}
}
