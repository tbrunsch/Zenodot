package dd.kms.zenodot.common;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Scans the methods of a class with three optional built-in filters.
 */
public class MethodScanner
{
	private Predicate<Method>	minimumAccessLevelFilter	= null;
	private Predicate<Method>	staticFilter				= null;
	private Predicate<Method>	nameFilter					= null;

	public MethodScanner minimumAccessLevel(AccessModifier minimumAccessLevel) {
		IntPredicate modifierFilter = ModifierFilters.createMinimumAccessLevelFilter(minimumAccessLevel);
		minimumAccessLevelFilter = method -> modifierFilter.test(method.getModifiers());
		return this;
	}

	public MethodScanner staticOnly(boolean staticOnly) {
		staticFilter = staticOnly ? method -> Modifier.isStatic(method.getModifiers()) : null;
		return this;
	}

	public MethodScanner name(String name) {
		nameFilter = method -> method.getName().equals(name);
		return this;
	}

	public List<Method> getMethods(Class<?> clazz) {
		Predicate<Method> filter = getFilter();
		Multimap<String, Class<?>[]> encounteredSignatures = ArrayListMultimap.create();

		List<Method> methods = new ArrayList<>();
		for (Class<?> curClazz = clazz; curClazz != null; curClazz = curClazz.getSuperclass()) {
			List<Method> declaredMethods = Arrays.stream(curClazz.getDeclaredMethods())
				.sorted(Comparator.comparing(method -> method.getName().toLowerCase()))	// sort methods because they are not guaranteed to be in any order
				.collect(Collectors.toList());

			for (Method method : Iterables.filter(declaredMethods, filter::test)) {
				String methodName = method.getName();
				Collection<Class<?>[]> encounteredArgumentTypeCombinations = encounteredSignatures.get(methodName);
				Class<?>[] argumentTypes = method.getParameterTypes();
				boolean isOverriden = encounteredArgumentTypeCombinations.stream().anyMatch(types -> Arrays.equals(argumentTypes, types));
				if (isOverriden) {
					continue;
				}
				encounteredSignatures.put(methodName, argumentTypes);
				methods.add(method);
			}
		}
		return methods;
	}

	private Predicate<Method> getFilter() {
		Predicate<Method> combinedFilter = null;
		for (Predicate<Method> filter : Arrays.asList(minimumAccessLevelFilter, staticFilter, nameFilter)) {
			if (filter == null) {
				continue;
			}
			combinedFilter = combinedFilter == null ? filter : combinedFilter.and(filter);
		}
		return combinedFilter == null ? field -> true : combinedFilter;
	}
}
