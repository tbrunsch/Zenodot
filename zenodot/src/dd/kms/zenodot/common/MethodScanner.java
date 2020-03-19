package dd.kms.zenodot.common;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
		Multimap<String, Method> methodsByName = ArrayListMultimap.create();
		addMethods(clazz, filter, methodsByName);
		return methodsByName.values()
			.stream()
			.sorted(new MemberComparator(clazz))
			.collect(Collectors.toList());
	}

	private static void addMethods(Class<?> clazz, Predicate<Method> filter, Multimap<String, Method> methodsByName) {
		if (clazz == null) {
			return;
		}
		for (Method method : clazz.getDeclaredMethods()) {
			if (!filter.test(method)) {
				continue;
			}
			String methodName = method.getName();
			Collection<Method> methodsWithSameName = methodsByName.get(methodName);
			Optional<Method> methodWithSameParameters = methodsWithSameName.stream()
				.filter(m -> methodsHaveSameArguments(m, method))
				.findFirst();
			if (!methodWithSameParameters.isPresent()) {
				methodsByName.put(methodName, method);
				continue;
			}
			Method otherMethod = methodWithSameParameters.get();
			if (methodIsMoreSpecific(method, otherMethod)) {
				methodsWithSameName.remove(otherMethod);
				methodsWithSameName.add(method);
			}
		}

		Class<?> superclass = clazz.getSuperclass();
		addMethods(superclass, filter, methodsByName);
		for (Class<?> implementedInterfaces :clazz.getInterfaces()){
			addMethods(implementedInterfaces, filter, methodsByName);
		}
	}

	private static boolean methodsHaveSameArguments(Method method, Method other) {
		return Arrays.equals(method.getParameterTypes(), other.getParameterTypes());
	}

	private static boolean methodIsMoreSpecific(Method method, Method other) {
		assert methodsHaveSameArguments(method, other);
		Class<?> declaringClass = method.getDeclaringClass();
		Class<?> otherDeclaringClass = other.getDeclaringClass();
		if (declaringClass.isAssignableFrom(otherDeclaringClass)) {
			return false;
		}
		if (otherDeclaringClass.isAssignableFrom(declaringClass)) {
			return true;
		}
		Class<?> returnType = method.getReturnType();
		Class<?> otherReturnType = other.getReturnType();
		if (returnType.isAssignableFrom(otherReturnType)) {
			return false;
		}
		assert otherReturnType.isAssignableFrom(returnType) : "Encountered two methods with same arguments, but incompatible return types";
		return true;
	}

	private Predicate<Method> getFilter() {
		return Filters.combine(minimumAccessLevelFilter, staticFilter, nameFilter);
	}
}
