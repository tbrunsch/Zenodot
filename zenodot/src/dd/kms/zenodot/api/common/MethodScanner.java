package dd.kms.zenodot.api.common;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Scans the methods of a class with three optional built-in filters.
 */
public interface MethodScanner
{
	List<Method> getMethods(Class<?> clazz);
}
