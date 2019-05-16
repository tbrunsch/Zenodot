package dd.kms.zenodot.common;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Scans the constructors of a class optionally considering a minimum access level.
 */
public class ConstructorScanner
{
	private Predicate<Constructor<?>>	minimumAccessLevelFilter	= null;

	public ConstructorScanner minimumAccessLevel(AccessModifier minimumAccessLevel) {
		IntPredicate modifierFilter = ModifierFilters.createMinimumAccessLevelFilter(minimumAccessLevel);
		minimumAccessLevelFilter = constructor -> modifierFilter.test(constructor.getModifiers());
		return this;
	}

	public List<Constructor<?>> getConstructors(Class<?> clazz) {
		Predicate<Constructor<?>> filter = getFilter();
		return Arrays.stream(clazz.getDeclaredConstructors())
			.filter(filter::test)
			.collect(Collectors.toList());
	}

	private Predicate<Constructor<?>> getFilter() {
		return minimumAccessLevelFilter == null ? field -> true : minimumAccessLevelFilter;
	}
}
