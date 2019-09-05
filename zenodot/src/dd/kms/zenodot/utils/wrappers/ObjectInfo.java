package dd.kms.zenodot.utils.wrappers;

/**
 * Wrapper class for objects<br/>
 * <br/>
 * Handles parameterized types (Generics) to some extend and keeps track of substituted parameters.<br/>
 * <br/>
 * Distinguishes between l-values (with setter) and r-values (without setter).<br/>
 * <br/>
 * An ObjectInfo can be created via
 * <ul>
 *     <li>{@link InfoProvider#createObjectInfo(Object)},</li>
 *     <li>{@link InfoProvider#createObjectInfo(Object, TypeInfo)}, and</li>
 *     <li>{@link InfoProvider#createObjectInfo(Object, TypeInfo, ValueSetter)}.</li>
 * </ul>
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
