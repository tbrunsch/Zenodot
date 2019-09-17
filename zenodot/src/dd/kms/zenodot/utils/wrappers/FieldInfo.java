package dd.kms.zenodot.utils.wrappers;

import dd.kms.zenodot.common.AccessModifier;

/**
 * Wrapper for fields<br/>
 * <br/>
 * Handles parameterized types (Generics) to some extend and keeps track of substituted parameters.
 */
public interface FieldInfo
{
	String getName();
	TypeInfo getType();
	boolean isStatic();
	boolean isFinal();
	AccessModifier getAccessModifier();
	TypeInfo getDeclaringType();
	Object get(Object instance) throws IllegalAccessException;
	void set(Object instance, Object value) throws IllegalAccessException;
}
