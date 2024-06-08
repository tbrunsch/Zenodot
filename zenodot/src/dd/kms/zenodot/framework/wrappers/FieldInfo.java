package dd.kms.zenodot.framework.wrappers;

import dd.kms.zenodot.api.common.AccessDeniedException;
import dd.kms.zenodot.api.common.GeneralizedField;

public class FieldInfo extends MemberInfo<GeneralizedField>
{
	public FieldInfo(GeneralizedField field) {
		super(field);
	}

	public GeneralizedField getField() {
		return member;
	}

	public Class<?> getType() {
		return member.getType();
	}

	public Object get(Object instance) throws AccessDeniedException {
		if (instance == InfoProvider.INDETERMINATE_VALUE) {
			return InfoProvider.INDETERMINATE_VALUE;
		}
		try {
			member.setAccessible(true);
			return member.get(instance);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new AccessDeniedException("Access to field '" + member.getName() + "' has been denied: " + e, e);
		}
	}

	public void set(Object instance, Object value) throws AccessDeniedException {
		try {
			member.setAccessible(true);
			member.set(instance, value);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new AccessDeniedException("Access to field '" + member.getName() + "' has been denied: " + e, e);
		}
	}

	@Override
	public String toString() {
		return getName();
	}
}
