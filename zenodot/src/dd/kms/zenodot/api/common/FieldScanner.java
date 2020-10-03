package dd.kms.zenodot.api.common;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Scans the fields of a class considering three optional built-in filters.
 */
public interface FieldScanner
{
	List<Field> getFields(Class<?> clazz);
}
