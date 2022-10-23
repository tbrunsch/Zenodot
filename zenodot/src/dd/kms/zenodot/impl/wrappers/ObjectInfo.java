package dd.kms.zenodot.impl.wrappers;

/**
 * Wrapper class for objects and classes.<br/>
 * <br/>
 * Distinguishes between l-values (with setter) and r-values (without setter).<br/>
 * <br/>
 * An ObjectInfo can be created via
 * <ul>
 *     <li>{@link InfoProvider#createObjectInfo(Object)},</li>
 *     <li>{@link InfoProvider#createObjectInfo(Object, Class)}, and</li>
 *     <li>{@link InfoProvider#createObjectInfo(Object, Class, ValueSetter)}.</li>
 * </ul>
 */
public class ObjectInfo
{
	private final Object		object;
	private final Class<?>		declaredType;
	private final ValueSetter	valueSetter;

	public ObjectInfo(Object object, Class<?> declaredType, ValueSetter valueSetter) {
		this.object = object;
		this.declaredType = declaredType;
		this.valueSetter = valueSetter;
	}

	public Object getObject() {
		return object;
	}

	public Class<?> getDeclaredType() {
		return declaredType;
	}

	public ValueSetter getValueSetter() {
		return valueSetter;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		String objectString =	object == null								? "NULL" :
								object == InfoProvider.INDETERMINATE_VALUE	? "indeterminate value"
																			: object.toString();
		builder.append(objectString);
		if (declaredType != InfoProvider.NO_TYPE) {
			String typeString =	declaredType.toString();
			builder.append(" (").append(typeString).append(")");
		}
		return builder.toString();
	}

	@FunctionalInterface
	public interface ValueSetter
	{
		void setObject(Object object) throws IllegalArgumentException;
	}
}
