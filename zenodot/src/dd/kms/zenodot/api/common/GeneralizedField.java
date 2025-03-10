package dd.kms.zenodot.api.common;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

/**
 * This class is an abstraction of the class {@link Field} that can also represent the field {@code length}
 * of an array. Except for that single case, it simply wraps a {@code Field} and delegates all methods to it.
 * For the sake of simplicity, this interface does not contain all methods provided by {@code Field}. If you
 * need more methods and the {@code FieldWrapper} is backed up by a {@code Field}, then you can obtain that
 * {@code Field} by calling {@link #getWrappedField()}. This method will only return {@code null} if the
 * instance represents the {@code length} field of an array.
 */
public interface GeneralizedField extends Member
{
	/**
	 * @return The underlying {@code Field}, if available, or {@code null} otherwise. Not that
	 * there is only one case in which there is no underlying field: If the {@code FieldWrapper}
	 * represents the field {@code length} of an array.
	 */
	@Nullable
	Field getWrappedField();

	/**
	 * @see Field#getDeclaringClass()
	 */
	@Override
	Class<?> getDeclaringClass();

	/**
	 * @see Field#getName()
	 */
	@Override
	String getName();

	/**
	 * @see Field#getModifiers()
	 */
	@Override
	int getModifiers();

	/**
	 * @see Field#isSynthetic()
	 */
	@Override
	boolean isSynthetic();

	/**
	 * @see Field#isEnumConstant()
	 */
	boolean isEnumConstant();

	/**
	 * @see Field#getType()
	 */
	Class<?> getType();

	/**
	 * @see Field#getGenericType()
	 */
	Type getGenericType();

	/**
	 * @see Field#get(Object)
	 */
	Object get(Object obj) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#getBoolean(Object)
	 */
	boolean getBoolean(Object obj) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#getByte(Object)
	 */
	byte getByte(Object obj) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#getChar(Object)
	 */
	char getChar(Object obj) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#getShort(Object)
	 */
	short getShort(Object obj) throws IllegalArgumentException, IllegalAccessException;;

	/**
	 * @see Field#getInt(Object)
	 */
	int getInt(Object obj) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#getLong(Object)
	 */
	long getLong(Object obj) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#getFloat(Object)
	 */
	float getFloat(Object obj) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#getDouble(Object)
	 */
	double getDouble(Object obj) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#set(Object, Object)
	 */
	void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#setBoolean(Object, boolean)
	 */
	void setBoolean(Object obj, boolean z) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#setByte(Object, byte)
	 */
	void setByte(Object obj, byte b) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#setChar(Object, char)
	 */
	void setChar(Object obj, char c) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#setShort(Object, short)
	 */
	void setShort(Object obj, short s) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#setInt(Object, int)
	 */
	void setInt(Object obj, int i) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#setLong(Object, long)
	 */
	void setLong(Object obj, long l) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#setFloat(Object, float)
	 */
	void setFloat(Object obj, float f) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see Field#setDouble(Object, double)
	 */
	void setDouble(Object obj, double d) throws IllegalArgumentException, IllegalAccessException;

	/**
	 * @see java.lang.reflect.AccessibleObject#isAccessible()
	 */
	boolean isAccessible();

	/**
	 * @see java.lang.reflect.AccessibleObject#setAccessible(boolean)
	 */
	void setAccessible(boolean flag) throws AccessDeniedException;

	/**
	 * @see Field#equals(Object)
	 */
	boolean equals(Object obj);

	/**
	 * @see Field#hashCode()
	 */
	int hashCode();

	/**
	 * @see Field#toString()
	 */
	String toString();
}
