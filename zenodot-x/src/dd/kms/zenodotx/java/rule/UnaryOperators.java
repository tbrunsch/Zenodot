package dd.kms.zenodotx.java.rule;

import java.util.function.Function;

public class UnaryOperators
{
	public static void registerPrefixOperatorsWithAssignment(UnaryOperatorRegistry registry) {
		registerPrefixOperatorWithAssignment(registry, "++", a -> (char) (a + 1), a -> (byte) (a + 1), a -> (short) (a + 1), a -> a + 1, a -> a + 1);
		registerPrefixOperatorWithAssignment(registry, "--", a -> (char) (a - 1), a -> (byte) (a - 1), a -> (short) (a - 1), a -> a - 1, a -> a - 1);
	}

	private static void registerPrefixOperatorWithAssignment(UnaryOperatorRegistry registry, String operator, Function<Character, Character> charImpl, Function<Byte, Byte> byteImpl, Function<Short, Short> shortImpl, Function<Integer, Integer> intImpl, Function<Long, Long> longImpl) {
		registerPrefixOperatorWithAssignment(registry, operator,	char.class,		charImpl);
		registerPrefixOperatorWithAssignment(registry, operator,	byte.class,		byteImpl);
		registerPrefixOperatorWithAssignment(registry, operator,	short.class,	shortImpl);
		registerPrefixOperatorWithAssignment(registry, operator,	int.class,		intImpl);
		registerPrefixOperatorWithAssignment(registry, operator,	long.class,		longImpl);
	}

	private static <S> void registerPrefixOperatorWithAssignment(UnaryOperatorRegistry registry, String operator, Class<S> operandClass, Function<S, S> implementation) {
		registry.register(operator, operandClass, operandClass, UnaryOperatorInfo.UnaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT, implementation);
	}

	public static void registerSignOperators(UnaryOperatorRegistry registry) {
		registerSignOperators(registry,	"+", a -> +a, a -> +a, a -> +a, a -> +a, a -> +a, a -> +a);
		registerSignOperators(registry,	"-", a -> -a, a -> -a, a -> -a, a -> -a, a -> -a, a -> -a);
	}

	private static void registerSignOperators(UnaryOperatorRegistry registry, String operator, Function<Byte, Integer> byteImpl, Function<Short, Integer> shortImpl, Function<Integer, Integer> intImpl, Function<Long, Long> longImpl, Function<Float, Float> floatImpl, Function<Double, Double> doubleImpl) {
		registerSignOperator(registry, operator,	byte.class,		int.class,		byteImpl);
		registerSignOperator(registry, operator,	short.class,	int.class,		shortImpl);
		registerSignOperator(registry, operator,	int.class,		int.class,		intImpl);
		registerSignOperator(registry, operator,	long.class,		long.class,		longImpl);
		registerSignOperator(registry, operator,	float.class,	float.class,	floatImpl);
		registerSignOperator(registry, operator,	double.class,	double.class,	doubleImpl);
	}

	private static <S, T> void registerSignOperator(UnaryOperatorRegistry registry, String operator, Class<S> operandClass, Class<T> resultClass, Function<S, T> implementation) {
		registry.register(operator, operandClass, resultClass, implementation);
	}

	public static void registerNegationOperators(UnaryOperatorRegistry registry) {
		registry.register("!", boolean.class, boolean.class, b -> !b);
		registry.register("~", int.class, int.class, i -> ~i);
		registry.register("~", long.class, long.class, l -> ~l);
	}
}
