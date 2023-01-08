package dd.kms.zenodot.impl.common;

import com.google.common.base.Preconditions;
import dd.kms.zenodot.api.common.GeneralizedField;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

class ArrayLengthField implements GeneralizedField
{
	private final Class<?>	clazz;

	ArrayLengthField(Class<?> clazz) {
		this.clazz = Preconditions.checkNotNull(clazz);
		Preconditions.checkArgument(clazz.isArray(), "Class " + clazz.getName() + " is no array");
	}

	@Override
	@Nullable
	public Field getWrappedField() {
		return null;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return clazz;
	}

	@Override
	public String getName() {
		return "length";
	}

	@Override
	public int getModifiers() {
		return Modifier.PUBLIC | Modifier.FINAL;
	}

	@Override
	public boolean isSynthetic() {
		return false;
	}

	@Override
	public boolean isEnumConstant() {
		return false;
	}

	@Override
	public Class<?> getType() {
		return int.class;
	}

	@Override
	public Type getGenericType() {
		return getType();
	}

	@Override
	public Object get(Object obj) throws IllegalArgumentException {
		checkInstance(obj);
		return getInt(obj);
	}

	@Override
	public boolean getBoolean(Object obj) throws IllegalArgumentException {
		checkInstance(obj);
		throw createWrongTypeException("boolean");
	}

	@Override
	public byte getByte(Object obj) throws IllegalArgumentException {
		checkInstance(obj);
		throw createWrongTypeException("byte");
	}

	@Override
	public char getChar(Object obj) throws IllegalArgumentException {
		checkInstance(obj);
		throw createWrongTypeException("char");
	}

	@Override
	public short getShort(Object obj) throws IllegalArgumentException {
		checkInstance(obj);
		throw createWrongTypeException("short");
	}

	@Override
	public int getInt(Object obj) throws IllegalArgumentException {
		checkInstance(obj);
		return Array.getLength(obj);
	}

	@Override
	public long getLong(Object obj) throws IllegalArgumentException {
		checkInstance(obj);
		return getInt(obj);
	}

	@Override
	public float getFloat(Object obj) throws IllegalArgumentException {
		checkInstance(obj);
		return getInt(obj);
	}

	@Override
	public double getDouble(Object obj) throws IllegalArgumentException {
		checkInstance(obj);
		return getInt(obj);
	}

	@Override
	public void set(Object obj, Object value) throws IllegalAccessException {
		checkInstance(obj);
		throw createModifyException();
	}

	@Override
	public void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException {
		checkInstance(obj);
		throw createModifyException();
	}

	@Override
	public void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException {
		checkInstance(obj);
		throw createModifyException();
	}

	@Override
	public void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException {
		checkInstance(obj);
		throw createModifyException();
	}

	@Override
	public void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException {
		checkInstance(obj);
		throw createModifyException();
	}

	@Override
	public void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException {
		checkInstance(obj);
		throw createModifyException();
	}

	@Override
	public void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException {
		checkInstance(obj);
		throw createModifyException();
	}

	@Override
	public void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException {
		checkInstance(obj);
		throw createModifyException();
	}

	@Override
	public void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException {
		checkInstance(obj);
		throw createModifyException();
	}

	@Override
	public void setAccessible(boolean flag) {
		/* nothing to do */
	}

	private void checkInstance(Object obj) {
		Preconditions.checkNotNull(obj);
		Preconditions.checkArgument(clazz.isInstance(obj), "The object is not of type " + clazz.getName());
	}

	private IllegalArgumentException createWrongTypeException(String type) {
		return new IllegalArgumentException("Field 'length' is not of type " + type);
	}

	private IllegalAccessException createModifyException() {
		return new IllegalAccessException("Cannot set value of field 'length'");
	}
}
