package dd.kms.zenodot.impl.wrappers;

import dd.kms.zenodot.api.common.AccessModifier;

public interface MemberInfo
{
	String getName();
	boolean isStatic();
	boolean isFinal();
	AccessModifier getAccessModifier();
	Class<?> getDeclaringClass();
}
