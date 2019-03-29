package dd.kms.zenodot.utils.wrappers;

import com.google.common.base.Preconditions;

/**
 * Wrapper class for objects<br/>
 * <br/>
 * Handles parameterized types (Generics) to some extend and keeps track of substituted parameters.<br/>
 * <br/>
 * Distinguishes between l-values and r-values.
 */
public class ObjectInfo
{
	public static final	Object		INDETERMINATE	= new Object();

	private final Object			object;
	private final TypeInfo			declaredType;
	private final ValueSetter valueSetter;

	public ObjectInfo(Object object, TypeInfo declaredType) {
		this(object, declaredType, null);
	}

	public ObjectInfo(Object object, TypeInfo declaredType, ValueSetter valueSetter) {
		this.object = object;
		this.declaredType = Preconditions.checkNotNull(declaredType);
		this.valueSetter = valueSetter;
	}

	public Object getObject() {
		return object;
	}

	public TypeInfo getDeclaredType() {
		return declaredType;
	}

	public ValueSetter getValueSetter() {
		return valueSetter;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(object == null ? "NULL" : object.toString());
		if (declaredType != TypeInfo.NONE) {
			builder.append(" (").append(declaredType.toString()).append(")");
		}
		return builder.toString();
	}

	@FunctionalInterface
	public interface ValueSetter
	{
		void setObject(Object object) throws IllegalArgumentException;
	}
}
