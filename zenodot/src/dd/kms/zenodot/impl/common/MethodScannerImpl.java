package dd.kms.zenodot.impl.common;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import dd.kms.zenodot.api.common.ExtensionMemberProvider;
import dd.kms.zenodot.api.common.GeneralizedMethod;
import dd.kms.zenodot.api.common.MethodScanner;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scans the methods of a class with three optional built-in filters.
 */
class MethodScannerImpl implements MethodScanner
{
	private final Predicate<GeneralizedMethod>		filter;
	private final Optional<ExtensionMemberProvider>	extensionMemberProvider;

	MethodScannerImpl(Predicate<GeneralizedMethod> filter, Optional<ExtensionMemberProvider> extensionMemberProvider) {
		this.filter = filter;
		this.extensionMemberProvider = extensionMemberProvider;
	}

	@Override
	public List<Method> getMethods(Class<?> clazz) {
		return getMethodsAndExtensionMethodsAsStream(clazz)
			.map(GeneralizedMethod::getWrappedMethod)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@Override
	public List<GeneralizedMethod> getMethodsAndExtensionMethods(Class<?> clazz) {
		return getMethodsAndExtensionMethodsAsStream(clazz).collect(Collectors.toList());
	}

	private Stream<GeneralizedMethod> getMethodsAndExtensionMethodsAsStream(Class<?> clazz) {
		Multimap<String, GeneralizedMethod> methodsByName = ArrayListMultimap.create();
		addMethods(clazz, methodsByName);
		return methodsByName.values()
			.stream()
			.sorted(new MemberComparator(clazz));
	}

	private void addMethods(Class<?> clazz, Multimap<String, GeneralizedMethod> methodsByName) {
		if (clazz == null) {
			return;
		}
		for (GeneralizedMethod method : getDeclaredMethods(clazz)) {
			if (!filter.test(method)) {
				continue;
			}
			String methodName = method.getName();
			Collection<GeneralizedMethod> methodsWithSameName = methodsByName.get(methodName);
			Optional<GeneralizedMethod> methodWithSameParameters = methodsWithSameName.stream()
				.filter(m -> methodsHaveSameArguments(m, method))
				.findFirst();
			if (!methodWithSameParameters.isPresent()) {
				methodsByName.put(methodName, method);
				continue;
			}
			GeneralizedMethod otherMethod = methodWithSameParameters.get();
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

	private Iterable<GeneralizedMethod> getDeclaredMethods(Class<?> clazz) {
		List<GeneralizedMethod> declaredMethods = Arrays.stream(clazz.getDeclaredMethods())
			.map(GeneralizedMethod::fromMethod)
			.collect(Collectors.toList());
		if (!extensionMemberProvider.isPresent()) {
			return declaredMethods;
		}
		List<GeneralizedMethod> extensionMethods = extensionMemberProvider.get().getExtensionMethodsFor(clazz);
		return Iterables.concat(declaredMethods, extensionMethods);
	}

	private static boolean methodsHaveSameArguments(GeneralizedMethod method, GeneralizedMethod other) {
		return Arrays.equals(method.getParameterTypes(), other.getParameterTypes());
	}

	private static boolean methodIsMoreSpecific(GeneralizedMethod method, GeneralizedMethod other) {
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
