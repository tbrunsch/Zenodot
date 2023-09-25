package dd.kms.zenodot.api.common;

import java.util.Objects;

/**
 * References a class by its fully qualified (normalized) name. That way, a class does not have to be loaded
 * until it is used.
 */
public class ClassInfo
{
	private final String normalizedClassName;

	public ClassInfo(String normalizedClassName) {
		this.normalizedClassName = normalizedClassName;
	}

	/**
	 * Returns the qualified class name as required for the reflection API. Inner class names are separated by
	 * a dollar sign ('$') from their outer class names.
	 */
	public String getNormalizedName() {
		return normalizedClassName;
	}

	/**
	 * Returns the qualified class name as used in Java code. Inner class names are separated by a dot ('.')
	 * from their outer class names.
	 */
	public String getRegularName() {
		return dd.kms.zenodot.impl.utils.ClassUtils.getRegularClassName(normalizedClassName);
	}

	/**
	 * Yields a class name without further qualification. This is in most cases the same
	 * as the simple class name. However, the name returned by this method may contain
	 * leading numbers (Java technicalities), whereas simple class names do not.
	 */
	public String getUnqualifiedName() {
		return dd.kms.zenodot.impl.utils.ClassUtils.getLeafOfPath(normalizedClassName);
	}

	/**
	 * Returns the {@link Class} object that is represented by this instance, if possible.
	 * @throws IllegalStateException if any exception occurred when loading the {@code Class}
	 */
	public Class<?> asClass() {
		try {
			return Class.forName(normalizedClassName);
		} catch (Throwable t) {
			dd.kms.zenodot.impl.utils.dataproviders.ClassDataProvider.reportClassWithError(this);
			throw new IllegalStateException("Error loading class '" + getRegularName() + "': " + t, t);
		}
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
		return getRegularName();
	}
}
