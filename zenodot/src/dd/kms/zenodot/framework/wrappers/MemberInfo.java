package dd.kms.zenodot.framework.wrappers;

import dd.kms.zenodot.api.common.AccessModifier;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Objects;

class MemberInfo<T extends Member>
{
	final T member;

	MemberInfo(T member) {
		this.member = member;
	}

	public String getName() {
		return member.getName();
	}

	public boolean isStatic() {
		return Modifier.isStatic(member.getModifiers());
	}

	public boolean isFinal() {
		return Modifier.isFinal(member.getModifiers());
	}

	public AccessModifier getAccessModifier() {
		return AccessModifier.getValue(member.getModifiers());
	}

	public Class<?> getDeclaringClass() {
		return member.getDeclaringClass();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MemberInfo<?> that = (MemberInfo<?>) o;
		return Objects.equals(member, that.member);
	}

	@Override
	public int hashCode() {
		return Objects.hash(member);
	}
}
