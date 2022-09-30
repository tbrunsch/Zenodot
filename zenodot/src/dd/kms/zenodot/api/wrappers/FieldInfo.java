package dd.kms.zenodot.api.wrappers;

/**
 * Wrapper for fields<br/>
 * <br/>
 * Handles parameterized types (Generics) to some extend and keeps track of substituted parameters.
 */
public interface FieldInfo extends MemberInfo
{
	Class<?> getType();
	Object get(Object instance) throws IllegalAccessException;
	void set(Object instance, Object value) throws IllegalAccessException;
}
