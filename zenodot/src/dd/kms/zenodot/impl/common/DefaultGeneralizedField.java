package dd.kms.zenodot.impl.common;

import com.google.common.base.Preconditions;
import dd.kms.zenodot.api.common.GeneralizedField;
import sun.reflect.CallerSensitive;

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
	@CallerSensitive
	public Object get(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.get(obj);
	}

	@Override
	@CallerSensitive
	public boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getBoolean(obj);
	}

	@Override
	@CallerSensitive
	public byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getByte(obj);
	}

	@Override
	@CallerSensitive
	public char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getChar(obj);
	}

	@Override
	@CallerSensitive
	public short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getShort(obj);
	}

	@Override
	@CallerSensitive
	public int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getInt(obj);
	}

	@Override
	@CallerSensitive
	public long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getLong(obj);
	}

	@Override
	@CallerSensitive
	public float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getFloat(obj);
	}

	@Override
	@CallerSensitive
	public double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException {
		return field.getDouble(obj);
	}

	@Override
	@CallerSensitive
	public void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException {
		field.set(obj, value);
	}

	@Override
	@CallerSensitive
	public void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException {
		field.setBoolean(obj, z);
	}

	@Override
	@CallerSensitive
	public void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException {
		field.setByte(obj, b);
	}

	@Override
	@CallerSensitive
	public void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException {
		field.setChar(obj, c);
	}

	@Override
	@CallerSensitive
	public void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException {
		field.setShort(obj, s);
	}

	@Override
	@CallerSensitive
	public void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException {
		field.setInt(obj, i);
	}

	@Override
	@CallerSensitive
	public void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException {
		field.setLong(obj, l);
	}

	@Override
	@CallerSensitive
	public void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException {
		field.setFloat(obj, f);
	}

	@Override
	@CallerSensitive
	public void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException {
		field.setDouble(obj, d);
	}

	@Override
	public void setAccessible(boolean flag) throws SecurityException {
		field.setAccessible(flag);
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
