package dd.kms.zenodot.utils.wrappers;

import dd.kms.zenodot.utils.ClassUtils;

import java.util.Objects;

class ClassInfoImpl implements ClassInfo
{
	private final String normalizedClassName;

	ClassInfoImpl(String normalizedClassName) {
		this.normalizedClassName = normalizedClassName;
	}

	@Override
	public String getNormalizedName() {
		return normalizedClassName;
	}

	@Override
	public String getUnqualifiedName() {
		return ClassUtils.getLeafOfPath(normalizedClassName);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClassInfoImpl that = (ClassInfoImpl) o;
		return Objects.equals(normalizedClassName, that.normalizedClassName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(normalizedClassName);
	}

	@Override
	public String toString() {
		return ClassUtils.getRegularClassName(normalizedClassName);
	}
}
