package dd.kms.zenodot.common;

import com.google.common.collect.Iterables;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
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
		Set<String> encounteredFieldNames = new HashSet<>();
		List<Field> fields = new ArrayList<>();
		for (Class<?> curClazz = clazz; curClazz != null; curClazz = curClazz.getSuperclass()) {
			List<Field> declaredFields = Arrays.stream(curClazz.getDeclaredFields())
				.filter(field -> !field.getName().startsWith("this$"))
				.sorted(Comparator.comparing(field -> field.getName().toLowerCase()))	// sort fields because they are not guaranteed to be in any order
				.collect(Collectors.toList());

			for (Field field : Iterables.filter(declaredFields, filter::test)) {
				if (filterShadowedFields) {
					String fieldName = field.getName();
					if (encounteredFieldNames.contains(fieldName)) {
						continue;
					}
					encounteredFieldNames.add(fieldName);
				}
				fields.add(field);
			}
		}
		return fields;
	}

	private Predicate<Field> getFilter() {
		Predicate<Field> combinedFilter = null;
		for (Predicate<Field> filter : Arrays.asList(minimumAccessLevelFilter, staticFilter, nameFilter)) {
			if (filter == null) {
				continue;
			}
			combinedFilter = combinedFilter == null ? filter : combinedFilter.and(filter);
		}
		return combinedFilter == null ? field -> true : combinedFilter;
	}
}
