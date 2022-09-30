package dd.kms.zenodot.impl.wrappers;

import com.google.common.base.Preconditions;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

public class ObjectInfoImpl implements ObjectInfo
{
	private final Object		object;
	private final Class<?>		declaredType;
	private final ValueSetter	valueSetter;

	public ObjectInfoImpl(Object object, Class<?> declaredType, ValueSetter valueSetter) {
		this.object = object;
		this.declaredType = declaredType;
		this.valueSetter = valueSetter;
	}

	@Override
	public Object getObject() {
		return object;
	}

	@Override
	public Class<?> getDeclaredType() {
		return declaredType;
	}

	@Override
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
}
