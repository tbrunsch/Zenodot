package dd.kms.zenodot.impl.wrappers;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.wrappers.FieldInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class FieldInfoImpl implements FieldInfo
{
	private final TypeInfo declaringType;
	private final Field		field;

	public FieldInfoImpl(TypeInfo declaringType, Field field) {
		this.declaringType = declaringType;
		this.field = field;
	}

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public TypeInfo getType() {
		return declaringType.resolveType(field.getGenericType());
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(field.getModifiers());
	}

	@Override
	public boolean isFinal() {
		return Modifier.isFinal(field.getModifiers());
	}

	@Override
	public AccessModifier getAccessModifier() {
		return AccessModifier.getValue(field.getModifiers());
	}

	@Override
	public TypeInfo getDeclaringType() {
		return declaringType;
	}

	@Override
	public Object get(Object instance) throws IllegalAccessException {
		field.setAccessible(true);
		return field.get(instance);
	}

	@Override
	public void set(Object instance, Object value) throws IllegalAccessException {
		field.setAccessible(true);
		field.set(instance, value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FieldInfoImpl fieldInfo = (FieldInfoImpl) o;
		return Objects.equals(field, fieldInfo.field) &&
				Objects.equals(declaringType, fieldInfo.declaringType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(field, declaringType);
	}

	@Override
	public String toString() {
		return getName();
	}
}
