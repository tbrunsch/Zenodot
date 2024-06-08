package dd.kms.zenodot.impl.common;

import com.google.common.base.Preconditions;
import dd.kms.zenodot.api.common.AccessDeniedException;
import dd.kms.zenodot.api.common.GeneralizedField;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Objects;

class DefaultGeneralizedField implements GeneralizedField
{
	private final Field field;

	DefaultGeneralizedField(Field field) {
		this.field = Preconditions.checkNotNull(field);
	}

	@Override
	@Nullable
	public Field getWrappedField() {
		return field;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return field.getDeclaringClass();
	}

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public int getModifiers() {
		return field.getModifiers();
	}

	@Override
	public boolean isEnumConstant() {
		return field.isEnumConstant();
	}

	@Override
	public boolean isSynthetic() {
		return field.isSynthetic();
	}

	@Override
	public Class<?> getType() {
		return field.getType();
	}

	@Override
	public Type getGenericType() {
		return field.getGenericType();
	}

	@Override
	public Object get(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.get(obj);
	}

	@Override
	public boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getBoolean(obj);
	}

	@Override
	public byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getByte(obj);
	}

	@Override
	public char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getChar(obj);
	}

	@Override
	public short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getShort(obj);
	}

	@Override
	public int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getInt(obj);
	}

	@Override
	public long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getLong(obj);
	}

	@Override
	public float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getFloat(obj);
	}

	@Override
	public double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getDouble(obj);
	}

	@Override
	public void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException {
		field.set(obj, value);
	}

	@Override
	public void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException {
		field.setBoolean(obj, z);
	}

	@Override
	public void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException {
		field.setByte(obj, b);
	}

	@Override
	public void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException {
		field.setChar(obj, c);
	}

	@Override
	public void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException {
		field.setShort(obj, s);
	}

	@Override
	public void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException {
		field.setInt(obj, i);
	}

	@Override
	public void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException {
		field.setLong(obj, l);
	}

	@Override
	public void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException {
		field.setFloat(obj, f);
	}

	@Override
	public void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException {
		field.setDouble(obj, d);
	}

	@Override
	public boolean isAccessible() {
		return field.isAccessible();
	}

	@Override
	public void setAccessible(boolean flag) throws AccessDeniedException {
		try {
			field.setAccessible(flag);
		} catch (Exception e) {
			throw new AccessDeniedException("Access to field '" + field.getName() + "' has been denied: " + e, e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DefaultGeneralizedField that = (DefaultGeneralizedField) o;
		return Objects.equals(field, that.field);
	}

	@Override
	public int hashCode() {
		return Objects.hash(field);
	}

	@Override
	public String toString() {
		return field.toString();
	}
}
