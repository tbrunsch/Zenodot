package dd.kms.zenodot.impl.wrappers;

import java.lang.reflect.Field;

public class FieldInfo extends MemberInfo<Field>
{
	public FieldInfo(Field field) {
		super(field);
	}

	public Field getField() {
		return member;
	}

	public Class<?> getType() {
		return member.getType();
	}

	public Object get(Object instance) throws IllegalAccessException {
		if (instance == InfoProvider.INDETERMINATE_VALUE) {
			return InfoProvider.INDETERMINATE_VALUE;
		}
		member.setAccessible(true);
		return member.get(instance);
	}

	public void set(Object instance, Object value) throws IllegalAccessException {
		member.setAccessible(true);
		member.set(instance, value);
	}

	@Override
	public String toString() {
		return getName();
	}
}
