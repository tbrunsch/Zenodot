package dd.kms.zenodot.impl.common;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dd.kms.zenodot.api.common.GeneralizedMethod;
import dd.kms.zenodot.api.common.MethodScanner;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Scans the methods of a class with three optional built-in filters.
 */
class MethodScannerImpl implements MethodScanner
{
	private final Predicate<Method>	filter;

	MethodScannerImpl(Predicate<Method> filter) {
		this.filter = filter;
	}

	@Override
	public List<Method> getMethods(Class<?> clazz) {
		Multimap<String, Method> methodsByName = ArrayListMultimap.create();
		addMethods(clazz, methodsByName);
		return methodsByName.values()
			.stream()
			.sorted(new MemberComparator(clazz))
			.collect(Collectors.toList());
	}

	private void addMethods(Class<?> clazz, Multimap<String, Method> methodsByName) {
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
		addMethods(superclass, methodsByName);
		for (Class<?> implementedInterfaces :clazz.getInterfaces()){
			addMethods(implementedInterfaces, methodsByName);
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
}
