package dd.kms.zenodot.api.common;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Scans the constructors of a class optionally considering a minimum access modifier.
 */
public class ConstructorScanner
{
	private Predicate<Constructor<?>>	minimumAccessModifierFilter	= null;

	public ConstructorScanner minimumAccessModifier(AccessModifier minimumAccessModifier) {
		IntPredicate modifierFilter = ModifierFilters.createMinimumAccessModifierFilter(minimumAccessModifier);
		minimumAccessModifierFilter = constructor -> modifierFilter.test(constructor.getModifiers());
		return this;
	}

	public List<Constructor<?>> getConstructors(Class<?> clazz) {
		Predicate<Constructor<?>> filter = getFilter();
		return Arrays.stream(clazz.getDeclaredConstructors())
			.filter(filter)
			.collect(Collectors.toList());
	}

	private Predicate<Constructor<?>> getFilter() {
		return Filters.nullToFilter(minimumAccessModifierFilter);
	}
}
