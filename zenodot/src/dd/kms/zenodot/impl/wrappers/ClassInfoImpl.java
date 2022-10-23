package dd.kms.zenodot.impl.wrappers;

import dd.kms.zenodot.impl.utils.ClassUtils;

import java.util.Objects;

public class ClassInfoImpl implements ClassInfo
{
	private final String normalizedClassName;

	public ClassInfoImpl(String normalizedClassName) {
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
