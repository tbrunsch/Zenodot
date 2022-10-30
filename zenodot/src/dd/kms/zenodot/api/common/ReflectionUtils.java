package dd.kms.zenodot.api.common;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.Primitives;

import java.lang.reflect.Method;
import java.util.function.Function;

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

	public static boolean isToStringMethod(Method method) {
		return method.getName().equals("toString")
			&& method.getParameterCount() == 0;
	}

	public static boolean isEqualsMethod(Method method) {
		return method.getName().equals("equals")
			&& method.getParameterCount() == 1
			&& method.getParameterTypes()[0] == Object.class;
	}

	public static boolean isHashCodeMethod(Method method) {
		return method.getName().equals("hashCode")
			&& method.getParameterCount() == 0;
	}
}
