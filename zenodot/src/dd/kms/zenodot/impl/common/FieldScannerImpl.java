package dd.kms.zenodot.impl.common;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.zenodot.api.common.FieldScanner;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scans the fields of a class considering three optional built-in filters.
 */
class FieldScannerImpl implements FieldScanner
{
	private final Predicate<Field>	filter;
	private final boolean			ignoreShadowedFields;

	FieldScannerImpl(Predicate<Field> filter, boolean ignoreShadowedFields) {
		this.filter = filter;
		this.ignoreShadowedFields = ignoreShadowedFields;
	}

	@Override
	public List<Field> getFields(Class<?> clazz) {
		Multimap<String, Field> fieldsByName = ArrayListMultimap.create();
		addFields(clazz, fieldsByName);
		return fieldsByName.values()
			.stream()
			.sorted(new MemberComparator(clazz))
			.collect(Collectors.toList());
	}

	private void addFields(Class<?> clazz, Multimap<String, Field> fieldsByName) {
		if (clazz == null) {
			return;
		}
		for (Field field : clazz.getDeclaredFields()) {
			if (!filter.test(field)) {
				continue;
			}
			String fieldName = field.getName();
			Collection<Field> fields = fieldsByName.get(fieldName);
			Iterator<Field> fieldIterator = fields.iterator();
			boolean addField = true;
			while (fieldIterator.hasNext()) {
				Field otherField = fieldIterator.next();
				Class<?> otherDeclaringClass = otherField.getDeclaringClass();
				if (otherDeclaringClass == clazz) {
					addField = false;
				} else if (clazz.isAssignableFrom(otherDeclaringClass)) {
					if (ignoreShadowedFields) {
						addField = false;
					}
				} else if (otherDeclaringClass.isAssignableFrom(clazz)) {
					if (ignoreShadowedFields) {
						fieldIterator.remove();
					}
				}
			}
			if (addField) {
				fields.add(field);
			}
		}

		Class<?> superclass = clazz.getSuperclass();
		addFields(superclass, fieldsByName);
		for (Class<?> implementedInterfaces :clazz.getInterfaces()){
			addFields(implementedInterfaces, fieldsByName);
		}
	}
}
