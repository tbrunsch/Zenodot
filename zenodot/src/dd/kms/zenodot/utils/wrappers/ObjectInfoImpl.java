package dd.kms.zenodot.utils.wrappers;

import com.google.common.base.Preconditions;

class ObjectInfoImpl implements ObjectInfo
{
	private final Object			object;
	private final TypeInfo			declaredType;
	private final ValueSetter valueSetter;

	ObjectInfoImpl(Object object, TypeInfo declaredType, ValueSetter valueSetter) {
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
		builder.append(object == null ? "NULL" : object.toString());
		if (declaredType != InfoProvider.NO_TYPE) {
			builder.append(" (").append(declaredType.toString()).append(")");
		}
		return builder.toString();
	}
}
