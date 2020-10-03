package dd.kms.zenodot.api.common;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Scans the constructors of a class optionally considering a minimum access modifier.
 */
public interface ConstructorScanner
{
	List<Constructor<?>> getConstructors(Class<?> clazz);
}
