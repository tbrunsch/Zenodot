package dd.kms.zenodot.api.wrappers;

import java.lang.reflect.Type;

/**
 * Wrapper class of Guava's {@link com.google.common.reflect.TypeToken} to minimize explicit dependencies
 * on the wrapped class throughout the code.
 */
public interface TypeInfo
{
	Class<?> getRawType();
	Type getType();
	TypeInfo getComponentType();
	boolean isPrimitive();
	boolean isArray();
	boolean isSupertypeOf(TypeInfo that);
	TypeInfo getSubtype(Class<?> subclass);
	TypeInfo resolveType(Type type);
	String getName();
	String getSimpleName();
}
