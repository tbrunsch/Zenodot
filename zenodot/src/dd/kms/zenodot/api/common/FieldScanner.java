package dd.kms.zenodot.api.common;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Scans the fields of a class considering three optional built-in filters.
 */
public class FieldScanner
{
	private Predicate<Field>	minimumAccessLevelFilter	= null;
	private Predicate<Field>	staticFilter				= null;
	private Predicate<Field>	nameFilter					= null;

	public FieldScanner minimumAccessLevel(AccessModifier minimumAccessLevel) {
		if (minimumAccessLevel == AccessModifier.PRIVATE) {
			minimumAccessLevelFilter = null;
		} else {
			IntPredicate modifierFilter = ModifierFilters.createMinimumAccessLevelFilter(minimumAccessLevel);
			minimumAccessLevelFilter = field -> modifierFilter.test(field.getModifiers());
		}
		return this;
	}

	public FieldScanner staticOnly(boolean staticOnly) {
		staticFilter = staticOnly ? field -> Modifier.isStatic(field.getModifiers()) : null;
		return this;
	}

	public FieldScanner name(String name) {
		nameFilter = field -> field.getName().equals(name);
		return this;
	}

	public List<Field> getFields(Class<?> clazz, boolean filterShadowedFields) {
		Predicate<Field> filter = getFilter();
		Multimap<String, Field> fieldsByName = ArrayListMultimap.create();
		addFields(clazz, filter, filterShadowedFields, fieldsByName);
		return fieldsByName.values()
			.stream()
			.sorted(new MemberComparator(clazz))
			.collect(Collectors.toList());
	}

	private void addFields(Class<?> clazz, Predicate<Field> filter, boolean filterShadowedFields, Multimap<String, Field> fieldsByName) {
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
					if (filterShadowedFields) {
						addField = false;
					}
				} else if (otherDeclaringClass.isAssignableFrom(clazz)) {
					if (filterShadowedFields) {
						fieldIterator.remove();
					}
				}
			}
			if (addField) {
				fields.add(field);
			}
		}

		Class<?> superclass = clazz.getSuperclass();
		addFields(superclass, filter, filterShadowedFields, fieldsByName);
		for (Class<?> implementedInterfaces :clazz.getInterfaces()){
			addFields(implementedInterfaces, filter, filterShadowedFields, fieldsByName);
		}
	}

	private Predicate<Field> getFilter() {
		Predicate<Field> specialClassFilter = field -> !field.getName().startsWith("this$");
		return Filters.combine(minimumAccessLevelFilter, staticFilter, nameFilter, specialClassFilter);
	}
}
