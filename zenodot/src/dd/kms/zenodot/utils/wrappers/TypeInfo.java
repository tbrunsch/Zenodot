package dd.kms.zenodot.utils.wrappers;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Wrapper class of Guava's {@link TypeToken} to minimize explicit dependencies
 * on the wrapped class throughout the code.
 */
public interface TypeInfo
{
	Class<?> getRawType();
	Type getType();
	TypeInfo getComponentType();
	boolean isPrimitive();
	boolean isSupertypeOf(TypeInfo that);
	TypeInfo getSubtype(Class<?> subclass);
	TypeInfo resolveType(Type type);
}
