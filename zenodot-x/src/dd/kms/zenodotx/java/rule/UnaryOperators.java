package dd.kms.zenodotx.java.rule;

import java.util.function.Function;

public class UnaryOperators
{
	public static void registerOperatorsWithAssignment(UnaryPrefixOperatorRegistry registry) {
		registerOperatorWithAssignment(registry, "++", a -> (char) (a + 1), a -> (byte) (a + 1), a -> (short) (a + 1), a -> a + 1, a -> a + 1);
		registerOperatorWithAssignment(registry, "--", a -> (char) (a - 1), a -> (byte) (a - 1), a -> (short) (a - 1), a -> a - 1, a -> a - 1);
	}

	private static void registerOperatorWithAssignment(UnaryPrefixOperatorRegistry registry, String operator, Function<Character, Character> charImpl, Function<Byte, Byte> byteImpl, Function<Short, Short> shortImpl, Function<Integer, Integer> intImpl, Function<Long, Long> longImpl) {
		registerOperatorWithAssignment(registry, operator,	char.class,		charImpl);
		registerOperatorWithAssignment(registry, operator,	byte.class,		byteImpl);
		registerOperatorWithAssignment(registry, operator,	short.class,	shortImpl);
		registerOperatorWithAssignment(registry, operator,	int.class,		intImpl);
		registerOperatorWithAssignment(registry, operator,	long.class,		longImpl);
	}

	private static <S> void registerOperatorWithAssignment(UnaryPrefixOperatorRegistry registry, String operator, Class<S> operandClass, Function<S, S> implementation) {
		registry.register(operator, operandClass, operandClass, implementation);
	}

	public static void registerSignOperators(UnaryPrefixOperatorRegistry registry) {
		registerSignOperators(registry,	"+", a -> +a, a -> +a, a -> +a, a -> +a, a -> +a, a -> +a);
		registerSignOperators(registry,	"-", a -> -a, a -> -a, a -> -a, a -> -a, a -> -a, a -> -a);
	}

	private static void registerSignOperators(UnaryPrefixOperatorRegistry registry, String operator, Function<Byte, Integer> byteImpl, Function<Short, Integer> shortImpl, Function<Integer, Integer> intImpl, Function<Long, Long> longImpl, Function<Float, Float> floatImpl, Function<Double, Double> doubleImpl) {
		registerSignOperator(registry, operator,	byte.class,		int.class,		byteImpl);
		registerSignOperator(registry, operator,	short.class,	int.class,		shortImpl);
		registerSignOperator(registry, operator,	int.class,		int.class,		intImpl);
		registerSignOperator(registry, operator,	long.class,		long.class,		longImpl);
		registerSignOperator(registry, operator,	float.class,	float.class,	floatImpl);
		registerSignOperator(registry, operator,	double.class,	double.class,	doubleImpl);
	}

	private static <S, T> void registerSignOperator(UnaryPrefixOperatorRegistry registry, String operator, Class<S> operandClass, Class<T> resultClass, Function<S, T> implementation) {
		registry.register(operator, operandClass, resultClass, implementation);
	}

	public static void registerNegationOperators(UnaryPrefixOperatorRegistry registry) {
		registry.register("!", boolean.class, boolean.class, b -> !b);
		registry.register("~", int.class, int.class, i -> ~i);
		registry.register("~", long.class, long.class, l -> ~l);
	}
}
