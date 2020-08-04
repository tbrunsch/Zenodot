package dd.kms.zenodot.impl.wrappers;

import com.google.common.base.Preconditions;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

public class ObjectInfoImpl implements ObjectInfo
{
	private final Object		object;
	private final TypeInfo declaredType;
	private final ValueSetter	valueSetter;

	public ObjectInfoImpl(Object object, TypeInfo declaredType, ValueSetter valueSetter) {
		this.object = object;
		this.declaredType = Preconditions.checkNotNull(declaredType);
		this.valueSetter = valueSetter;
	}

	@Override
	public Object getObject() {
		return object;
	}

	@Override
	public TypeInfo getDeclaredType() {
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
			String typeString =	declaredType == InfoProvider.UNKNOWN_TYPE	? "unknown type"
																			: declaredType.toString();
			builder.append(" (").append(typeString).append(")");
		}
		return builder.toString();
	}
}
