package dd.kms.zenodot.api.common;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface ExtensionMethodBody
{
	Object execute(Object obj, Object... args) throws InvocationTargetException;
}
