package dd.kms.zenodot.api.common;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.Primitives;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReflectionUtils
{
	/*
	 * Type Conversions
	 */
	private static final Table<Class<?>, Class<?>, Function<Object, Object>>	PRIMITIVE_CONVERSIONS 			= HashBasedTable.create();
	private static final Table<Class<?>, Class<?>, Function<Object, Object>>	PRIMITIVE_NARROWING_CONVERSIONS	= HashBasedTable.create();

	private static <S, T> void addConversion(Class<S> sourceClass, Class<T> targetClass, Function<S, T> conversion) {
		PRIMITIVE_CONVERSIONS.put(sourceClass, targetClass, value -> conversion.apply((S) value));
	}

	private static <T, S> void addNarrowingConversion(Class<T> sourceClass, Class<S> targetClass, Function<T, S> narrowingConversion) {
		PRIMITIVE_NARROWING_CONVERSIONS.put(sourceClass, targetClass, value -> narrowingConversion.apply((T) value));
	}

	private static <S> void addPrimitiveBoxedConversions(Class<S> primitiveClass, Class<S> boxedClass, Function<S, S> conversion) {
		addConversion(primitiveClass, primitiveClass, 	conversion);
		addConversion(primitiveClass, boxedClass,		conversion);
		addConversion(boxedClass, primitiveClass, 		conversion);
		addConversion(boxedClass, boxedClass, 			conversion);
	}

	private static <S, T> void addConversions(Class<S> primitiveSourceClass, Class<T> primitiveTargetClass, Function<S, T> conversion, Function<T, S> narrowingConversion) {
		Class<S> boxedSourceClass = Primitives.wrap(primitiveSourceClass);
		Class<T> boxedTargetClass = Primitives.wrap(primitiveTargetClass);
		addConversion(primitiveSourceClass, primitiveTargetClass, 	conversion);
		addConversion(primitiveSourceClass, boxedTargetClass, 		conversion);
		addConversion(boxedSourceClass, 	primitiveTargetClass, 	conversion);
		addConversion(boxedSourceClass, 	boxedTargetClass, 		conversion);
		addNarrowingConversion(primitiveTargetClass, 	primitiveSourceClass,	narrowingConversion);
		addNarrowingConversion(boxedTargetClass, 		primitiveSourceClass,	narrowingConversion);
		addNarrowingConversion(primitiveTargetClass, 	boxedSourceClass,		narrowingConversion);
		addNarrowingConversion(boxedTargetClass, 		boxedSourceClass,		narrowingConversion);
	}

	public static boolean isPrimitiveConvertibleTo(Class<?> sourceClass, Class<?> targetClass, boolean allowNarrowing) {
		return PRIMITIVE_CONVERSIONS.contains(sourceClass, targetClass)
				|| (allowNarrowing && PRIMITIVE_NARROWING_CONVERSIONS.contains(sourceClass, targetClass));
	}

	public static <T> T convertTo(Object value, Class<T> targetClass, boolean allowNarrowing) {
		if (value == null) {
			return null;
		}
		if (targetClass.isInstance(value)) {
			return targetClass.cast(value);
		}
		Class<?> sourceClass = value.getClass();
		if (PRIMITIVE_CONVERSIONS.contains(sourceClass, targetClass)) {
			Function<Object, Object> conversion = PRIMITIVE_CONVERSIONS.get(sourceClass, targetClass);
			return (T) conversion.apply(value);
		}
		if (allowNarrowing && PRIMITIVE_NARROWING_CONVERSIONS.contains(sourceClass, targetClass)) {
			Function<Object, Object> conversion = PRIMITIVE_NARROWING_CONVERSIONS.get(sourceClass, targetClass);
			return (T) conversion.apply(value);
		}
		throw new ClassCastException("Cannot cast '" + sourceClass.getSimpleName() + "' to '" + targetClass + "'");
	}

	static {
		addPrimitiveBoxedConversions(byte.class,	Byte.class,			b -> b.byteValue());
		addPrimitiveBoxedConversions(short.class,	Short.class,		s -> s.shortValue());
		addPrimitiveBoxedConversions(int.class,		Integer.class,		i -> i.intValue());
		addPrimitiveBoxedConversions(long.class,	Long.class,			l -> l.longValue());
		addPrimitiveBoxedConversions(float.class,	Float.class,		f -> f.floatValue());
		addPrimitiveBoxedConversions(double.class,	Double.class,		d -> d.doubleValue());
		addPrimitiveBoxedConversions(boolean.class,	Boolean.class,		b -> b.booleanValue());
		addPrimitiveBoxedConversions(char.class,	Character.class,	c -> c.charValue());
		addPrimitiveBoxedConversions(void.class,	Void.class,			v -> null);

		addConversions(byte.class,	short.class, 	b -> b.shortValue(),	s -> s.byteValue());
		addConversions(byte.class,	int.class, 		b -> b.intValue(),		i -> i.byteValue());
		addConversions(byte.class,	long.class, 	b -> b.longValue(),		l -> l.byteValue());
		addConversions(byte.class,	float.class, 	b -> b.floatValue(),	f -> f.byteValue());
		addConversions(byte.class,	double.class,	b -> b.doubleValue(),	d -> d.byteValue());

		addConversions(short.class,	int.class,		s -> s.intValue(),		i -> i.shortValue());
		addConversions(short.class,	long.class,		s -> s.longValue(),		l -> l.shortValue());
		addConversions(short.class,	float.class,	s -> s.floatValue(),	f -> f.shortValue());
		addConversions(short.class,	double.class,	s -> s.doubleValue(),	d -> d.shortValue());

		addConversions(int.class,	long.class,		i -> i.longValue(),		l -> l.intValue());
		addConversions(int.class,	float.class,	i -> i.floatValue(),	f -> f.intValue());
		addConversions(int.class,	double.class,	i -> i.doubleValue(),	d -> d.intValue());

		addConversions(long.class,	float.class,	l -> l.floatValue(),	f -> f.longValue());
		addConversions(long.class,	double.class,	l -> l.doubleValue(),	d -> d.longValue());

		addConversions(float.class,	double.class,	f -> f.doubleValue(),	d -> d.floatValue());

		addConversions(char.class,	int.class, 		c -> (int) 		c.charValue(),	i -> (char) i.intValue());
		addConversions(char.class,	long.class, 	c -> (long) 	c.charValue(),	l -> (char) l.longValue());
		addConversions(char.class,	float.class, 	c -> (float) 	c.charValue(),	f -> (char) f.floatValue());
		addConversions(char.class,	double.class, 	c -> (double) 	c.charValue(),	d -> (char) d.doubleValue());
	}

	public static boolean isFunctionalInterface(Class<?> clazz) {
		if (!clazz.isInterface()) {
			return false;
		}
		List<Method> unimplementedMethods = getUnimplementedMethods(clazz);
		return unimplementedMethods.size() == 1;
	}

	public static List<Method> getUnimplementedMethods(Class<?> clazz) {
		MethodScanner scanner = MethodScannerBuilder.create().staticMode(StaticMode.NON_STATIC).build();
		List<Method> methods = scanner.getMethods(clazz);
		return methods.stream()
			.filter(method -> !method.isDefault())
			.filter(method -> Modifier.isAbstract(method.getModifiers()))
			.filter(method -> !isToStringMethod(method))
			.filter(method -> !isEqualsMethod(method))
			.filter(method -> !isHashCodeMethod(method))
			.collect(Collectors.toList());
	}

	private static boolean isToStringMethod(Method method) {
		return method.getName().equals("toString")
			&& method.getParameterCount() == 0;
	}

	private static boolean isEqualsMethod(Method method) {
		return method.getName().equals("equals")
			&& method.getParameterCount() == 1
			&& method.getParameterTypes()[0] == Object.class;
	}

	private static boolean isHashCodeMethod(Method method) {
		return method.getName().equals("hashCode")
			&& method.getParameterCount() == 0;
	}

	public static Collection<Method> getBaseMethods(Method method) {
		Set<Method> baseMethods = new LinkedHashSet<>();
		addBaseMethods(method, baseMethods);
		return baseMethods;
	}

	private static void addBaseMethods(Method method, Set<Method> baseMethods) {
		Class<?> declaringClass = method.getDeclaringClass();
		Class<?> superClass = declaringClass.getSuperclass();
		if (superClass != null) {
			addBaseMethods(method, superClass, baseMethods);
		}
		Class<?>[] interfaces = declaringClass.getInterfaces();
		for (Class<?> implementedInterface : interfaces) {
			addBaseMethods(method, implementedInterface, baseMethods);
		}
	}

	private static void addBaseMethods(Method method, Class<?> superType, Set<Method> baseMethods) {
		String methodName = method.getName();
		Method[] declaredMethods = superType.getDeclaredMethods();
		for (Method declaredMethod : declaredMethods) {
			if (!Objects.equals(declaredMethod.getName(), methodName)) {
				continue;
			}
			if (isOverriddenBy(declaredMethod, method)) {
				if (baseMethods.add(declaredMethod)) {
					addBaseMethods(declaredMethod, baseMethods);
				}
			}
		}
	}

	public static boolean isOverriddenBy(Executable baseExecutable, Executable potentialOverride) {
		Class<?> executableClass = baseExecutable.getClass();
		if (!Objects.equals(executableClass, potentialOverride.getClass())) {
			return false;
		}
		int baseModifiers = baseExecutable.getModifiers();
		if (Modifier.isStatic(baseModifiers)) {
			return false;
		}
		int potentialOverrideModifiers = potentialOverride.getModifiers();
		if (Modifier.isStatic(potentialOverrideModifiers)) {
			return false;
		}
		AccessModifier baseAccessModifier = AccessModifier.getValue(baseModifiers);
		AccessModifier potentialOverrideAccessModifier = AccessModifier.getValue(potentialOverrideModifiers);
		if (baseAccessModifier.compareTo(potentialOverrideAccessModifier) < 0) {
			return false;
		}

		if (baseExecutable.equals(potentialOverride)) {
			return true;
		}
		if (Objects.equals(executableClass, Constructor.class)) {
			return false;
		}
		if (potentialOverride instanceof Constructor) {
			return false;
		}

		if (baseExecutable.isSynthetic() && baseExecutable instanceof Method && !(((Method) baseExecutable).isBridge())) {
			return false;
		}
		if (potentialOverride.isSynthetic() && potentialOverride instanceof Method && !(((Method) potentialOverride).isBridge())) {
			return false;
		}

		/*
		 * We would like to check whether baseExecutable.isVarArgs() == potentialOverride.isVarArgs().
		 * However, for some reason, JimfsFileSystem.getPath() was considered var args in debug mode,
		 * but not in release mode. As a consequence, this method did not detect that this method
		 * overrides FileSystem.getPath(). As a workaround, we removed that check.
		 */

		if (!baseExecutable.getName().equals(potentialOverride.getName())) {
			return false;
		}

		Class<?> baseClass = baseExecutable.getDeclaringClass();
		Class<?> potentialSubClass = potentialOverride.getDeclaringClass();
		if (Objects.equals(baseClass, potentialSubClass) || !baseClass.isAssignableFrom(potentialSubClass)) {
			return false;
		}

		final AccessModifier requiredBaseVisibility;
		if (isInnerClassOf(potentialSubClass, baseClass)) {
			requiredBaseVisibility = AccessModifier.PRIVATE;
		} else {
			Package basePackage = baseClass.getPackage();
			Package potentialSubClassPackage = potentialSubClass.getPackage();
			requiredBaseVisibility = Objects.equals(basePackage, potentialSubClassPackage)
				? AccessModifier.PACKAGE_PRIVATE
				: AccessModifier.PROTECTED;
		}
		if (requiredBaseVisibility.compareTo(baseAccessModifier) < 0) {
			return false;
		}

		Class<?>[] baseMethodParameters = baseExecutable.getParameterTypes();
		Class<?>[] potentialOverrideParameters = potentialOverride.getParameterTypes();
		return Arrays.equals(baseMethodParameters, potentialOverrideParameters);
	}

	private static boolean isInnerClassOf(Class<?> clazz, Class<?> potentialEnclosingClass) {
		while (clazz != null) {
			if (Objects.equals(clazz, potentialEnclosingClass)) {
				return true;
			}
			clazz = clazz.getEnclosingClass();
		}
		return false;
	}

	public static boolean tryMakeAccessible(AccessibleObject accessibleObject) {
		if (accessibleObject.isAccessible()) {
			return true;
		}
		try {
			accessibleObject.setAccessible(true);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean tryMakeAccessible(GeneralizedField generalizedField) {
		if (generalizedField.isAccessible()) {
			return true;
		}
		try {
			generalizedField.setAccessible(true);
			return true;
		} catch (AccessDeniedException e) {
			return false;
		}
	}
}
