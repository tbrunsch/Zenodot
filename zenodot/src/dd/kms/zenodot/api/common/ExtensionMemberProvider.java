package dd.kms.zenodot.api.common;

import java.util.List;

public interface ExtensionMemberProvider
{
	List<GeneralizedMethod> getExtensionMethodsFor(Class<?> clazz);
}
