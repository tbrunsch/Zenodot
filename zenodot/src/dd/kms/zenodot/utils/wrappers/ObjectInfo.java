package dd.kms.zenodot.utils.wrappers;

/**
 * Wrapper class for objects<br/>
 * <br/>
 * Handles parameterized types (Generics) to some extend and keeps track of substituted parameters.<br/>
 * <br/>
 * Distinguishes between l-values and r-values.
 */
public interface ObjectInfo
{
	Object getObject();
	TypeInfo getDeclaredType();
	ValueSetter getValueSetter();

	@FunctionalInterface
	interface ValueSetter
	{
		void setObject(Object object) throws IllegalArgumentException;
	}
}
