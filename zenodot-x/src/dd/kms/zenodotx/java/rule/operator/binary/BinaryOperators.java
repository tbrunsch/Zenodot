package dd.kms.zenodotx.java.rule.operator.binary;

import dd.kms.zenodotx.common.NullableOptional;

import java.util.function.BiFunction;
import java.util.function.Function;

public class BinaryOperators
{
	public static void registerBinaryOperators12(BinaryOperatorRegistry registry) {
		registerNumericOperator(registry, "*", (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b);
		registerNumericOperator(registry, "/", (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b);
		registerNumericOperator(registry, "%", (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b);
	}

	public static void registerBinaryOperators11(BinaryOperatorRegistry registry) {
		registerNumericOperator(registry, "+", (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b);
		registerNumericOperator(registry, "-", (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b);

		// String concatenation
		registerOperator(registry, "+", String.class, String.class, String.class, (s1, s2) -> s1 + s2);
		registerOperator(registry, "+", String.class, Object.class, String.class, (s1, s2) -> s1 + s2);
		registerOperator(registry, "+", Object.class, String.class, String.class, (s1, s2) -> s1 + s2);
	}

	public static void registerBinaryOperators10(BinaryOperatorRegistry registry) {
		registerShiftOperator(registry, "<<",	(a, b) -> a << b,	(a, b) -> a << b,	(a, b) -> a << b,	(a, b) -> a << b,	(a, b) -> a << b);
		registerShiftOperator(registry, ">>",	(a, b) -> a >> b,	(a, b) -> a >> b,	(a, b) -> a >> b,	(a, b) -> a >> b,	(a, b) -> a >> b);
		registerShiftOperator(registry, ">>>",	(a, b) -> a >>> b,	(a, b) -> a >>> b,	(a, b) -> a >>> b,	(a, b) -> a >>> b,	(a, b) -> a >>> b);
	}

	public static void registerBinaryOperators9(BinaryOperatorRegistry registry) {
		registerComparisonOperator(registry, "<",	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b,	(a, b) -> a < b);
		registerComparisonOperator(registry, "<=",	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b,	(a, b) -> a <= b);
		registerComparisonOperator(registry, ">",	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b,	(a, b) -> a > b);
		registerComparisonOperator(registry, ">=",	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b,	(a, b) -> a >= b);
	}

	public static void registerBinaryOperators8(BinaryOperatorRegistry registry) {
		// Comparison of primitives: We must explicitly unbox to prevent comparison of references
		registerComparisonOperator(registry, "==", (a, b) -> a.booleanValue() == b.booleanValue(), (a, b) -> a.charValue() == b.charValue(), (a, b) -> a.byteValue() == b.byteValue(), (a, b) -> a.shortValue() == b.shortValue(), (a, b) -> a.intValue() == b.intValue(), (a, b) -> a.longValue() == b.longValue(), (a, b) -> a.floatValue() == b.floatValue(), (a, b) -> a.doubleValue() == b.doubleValue());
		registerComparisonOperator(registry, "!=", (a, b) -> a.booleanValue() != b.booleanValue(), (a, b) -> a.charValue() != b.charValue(), (a, b) -> a.byteValue() != b.byteValue(), (a, b) -> a.shortValue() != b.shortValue(), (a, b) -> a.intValue() != b.intValue(), (a, b) -> a.longValue() != b.longValue(), (a, b) -> a.floatValue() != b.floatValue(), (a, b) -> a.doubleValue() != b.doubleValue());

		// Comparison of objects
		registerOperator(registry, "==", Object.class, Object.class, boolean.class, (a, b) -> a == b);
		registerOperator(registry, "!=", Object.class, Object.class, boolean.class, (a, b) -> a != b);
	}

	public static void registerBinaryOperators7(BinaryOperatorRegistry registry) {
		registerBitOperator(registry, "&", (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b);
	}

	public static void registerBinaryOperators6(BinaryOperatorRegistry registry) {
		registerBitOperator(registry, "^", (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b);
	}

	public static void registerBinaryOperators5(BinaryOperatorRegistry registry) {
		registerBitOperator(registry, "|", (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b);
	}

	public static void registerBinaryOperators4(BinaryOperatorRegistry registry) {
		registerOperator(registry, "&&", boolean.class, boolean.class, boolean.class, BinaryOperatorMode.RETURN_RESULT, (a, b) -> a && b, a -> a ? NullableOptional.empty() : NullableOptional.of(false));
	}

	public static void registerBinaryOperators3(BinaryOperatorRegistry registry) {
		registerOperator(registry, "||", boolean.class, boolean.class, boolean.class, BinaryOperatorMode.RETURN_RESULT, (a, b) -> a || b, a -> a ? NullableOptional.of(true) : NullableOptional.empty());
	}

	public static void registerBinaryOperators1(BinaryOperatorRegistry registry) {
		registerOperator(registry, "=", Object.class, Object.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b, null, (classA, classB) -> classB);

		registerOperator(registry, "=", char.class, char.class, char.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);

		registerOperator(registry, "=", byte.class, byte.class, byte.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);

		registerOperator(registry, "=", short.class, byte.class,	byte.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", short.class, short.class,	short.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);

		registerOperator(registry, "=", int.class, char.class,	char.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", int.class, byte.class,	byte.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", int.class, int.class,	int.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);

		registerOperator(registry, "=", long.class, char.class,	char.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", long.class, byte.class,	byte.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", long.class, int.class,	int.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", long.class, long.class,	long.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);

		registerOperator(registry, "=", float.class, char.class,	char.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", float.class, byte.class,	byte.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", float.class, int.class,		int.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", float.class, long.class,	long.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", float.class, float.class,	float.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);

		registerOperator(registry, "=", double.class, char.class,	char.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", double.class, byte.class,	byte.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", double.class, int.class,	int.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", double.class, long.class,	long.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", double.class, float.class,	float.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);
		registerOperator(registry, "=", double.class, double.class,	double.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b);

		registerOperatorWithAssignment(registry, "+=", char.class,		(a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b);
		registerOperatorWithAssignment(registry, "+=", byte.class,		(a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b);
		registerOperatorWithAssignment(registry, "+=", short.class,		(a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b);
		registerOperatorWithAssignment(registry, "+=", int.class,		(a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b);
		registerOperatorWithAssignment(registry, "+=", long.class,		(a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b);
		registerOperatorWithAssignment(registry, "+=", float.class,		(a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b);
		registerOperatorWithAssignment(registry, "+=", double.class,	(a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b, (a, b) -> a + b);

		registerOperatorWithAssignment(registry, "-=", char.class,		(a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b);
		registerOperatorWithAssignment(registry, "-=", byte.class,		(a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b);
		registerOperatorWithAssignment(registry, "-=", short.class,		(a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b);
		registerOperatorWithAssignment(registry, "-=", int.class,		(a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b);
		registerOperatorWithAssignment(registry, "-=", long.class,		(a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b);
		registerOperatorWithAssignment(registry, "-=", float.class,		(a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b);
		registerOperatorWithAssignment(registry, "-=", double.class,	(a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b, (a, b) -> a - b);

		registerOperatorWithAssignment(registry, "*=", char.class,		(a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b);
		registerOperatorWithAssignment(registry, "*=", byte.class,		(a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b);
		registerOperatorWithAssignment(registry, "*=", short.class,		(a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b);
		registerOperatorWithAssignment(registry, "*=", int.class,		(a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b);
		registerOperatorWithAssignment(registry, "*=", long.class,		(a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b);
		registerOperatorWithAssignment(registry, "*=", float.class,		(a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b);
		registerOperatorWithAssignment(registry, "*=", double.class,	(a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b, (a, b) -> a * b);

		registerOperatorWithAssignment(registry, "/=", char.class,		(a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b);
		registerOperatorWithAssignment(registry, "/=", byte.class,		(a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b);
		registerOperatorWithAssignment(registry, "/=", short.class,		(a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b);
		registerOperatorWithAssignment(registry, "/=", int.class,		(a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b);
		registerOperatorWithAssignment(registry, "/=", long.class,		(a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b);
		registerOperatorWithAssignment(registry, "/=", float.class,		(a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b);
		registerOperatorWithAssignment(registry, "/=", double.class,	(a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b, (a, b) -> a / b);

		registerOperatorWithAssignment(registry, "%=", char.class,		(a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b);
		registerOperatorWithAssignment(registry, "%=", byte.class,		(a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b);
		registerOperatorWithAssignment(registry, "%=", short.class,		(a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b);
		registerOperatorWithAssignment(registry, "%=", int.class,		(a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b);
		registerOperatorWithAssignment(registry, "%=", long.class,		(a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b);
		registerOperatorWithAssignment(registry, "%=", float.class,		(a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b);
		registerOperatorWithAssignment(registry, "%=", double.class,	(a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b, (a, b) -> a % b);

		registerOperatorWithAssignment(registry, "&=", char.class,		(a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b);
		registerOperatorWithAssignment(registry, "&=", byte.class,		(a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b);
		registerOperatorWithAssignment(registry, "&=", short.class,		(a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b);
		registerOperatorWithAssignment(registry, "&=", int.class,		(a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b);
		registerOperatorWithAssignment(registry, "&=", long.class,		(a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b, (a, b) -> a & b);

		registerOperatorWithAssignment(registry, "^=", char.class,		(a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b);
		registerOperatorWithAssignment(registry, "^=", byte.class,		(a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b);
		registerOperatorWithAssignment(registry, "^=", short.class,		(a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b);
		registerOperatorWithAssignment(registry, "^=", int.class,		(a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b);
		registerOperatorWithAssignment(registry, "^=", long.class,		(a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b, (a, b) -> a ^ b);

		registerOperatorWithAssignment(registry, "|=", char.class,		(a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b);
		registerOperatorWithAssignment(registry, "|=", byte.class,		(a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b);
		registerOperatorWithAssignment(registry, "|=", short.class,		(a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b);
		registerOperatorWithAssignment(registry, "|=", int.class,		(a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b);
		registerOperatorWithAssignment(registry, "|=", long.class,		(a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b, (a, b) -> a | b);

		registerOperatorWithAssignment(registry, "<<=", char.class,		(a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b);
		registerOperatorWithAssignment(registry, "<<=", byte.class,		(a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b);
		registerOperatorWithAssignment(registry, "<<=", short.class,	(a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b);
		registerOperatorWithAssignment(registry, "<<=", int.class,		(a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b);
		registerOperatorWithAssignment(registry, "<<=", long.class,		(a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b, (a, b) -> a << b);

		registerOperatorWithAssignment(registry, ">>=", char.class,		(a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b);
		registerOperatorWithAssignment(registry, ">>=", byte.class,		(a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b);
		registerOperatorWithAssignment(registry, ">>=", short.class,	(a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b);
		registerOperatorWithAssignment(registry, ">>=", int.class,		(a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b);
		registerOperatorWithAssignment(registry, ">>=", long.class,		(a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b, (a, b) -> a >> b);

		registerOperatorWithAssignment(registry, ">>>=", char.class,	(a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b);
		registerOperatorWithAssignment(registry, ">>>=", byte.class,	(a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b);
		registerOperatorWithAssignment(registry, ">>>=", short.class,	(a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b);
		registerOperatorWithAssignment(registry, ">>>=", int.class,		(a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b);
		registerOperatorWithAssignment(registry, ">>>=", long.class,	(a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b, (a, b) -> a >>> b);
	}

	private static void registerNumericOperator(BinaryOperatorRegistry registry, String operator, BiFunction<Character, Character, Integer> charImpl, BiFunction<Byte, Byte, Integer> byteImpl, BiFunction<Short, Short, Integer> shortImpl, BiFunction<Integer, Integer, Integer> intImpl, BiFunction<Long, Long, Long> longImpl, BiFunction<Float, Float, Float> floatImpl, BiFunction<Double, Double, Double> doubleImpl) {
		registerOperator(registry, operator,	char.class,		int.class,		charImpl);
		registerOperator(registry, operator,	byte.class,		int.class,		byteImpl);
		registerOperator(registry, operator,	short.class,	int.class,		shortImpl);
		registerOperator(registry, operator,	int.class,		int.class,		intImpl);
		registerOperator(registry, operator,	long.class,		long.class,		longImpl);
		registerOperator(registry, operator,	float.class,	float.class,	floatImpl);
		registerOperator(registry, operator,	double.class,	double.class,	doubleImpl);
	}

	private static void registerShiftOperator(BinaryOperatorRegistry registry, String operator, BiFunction<Character, Long, Integer> charImpl, BiFunction<Byte, Long, Integer> byteImpl, BiFunction<Short, Long, Integer> shortImpl, BiFunction<Integer, Long, Integer> intImpl, BiFunction<Long, Long, Long> longImpl) {
		registerOperator(registry, operator, char.class, 	long.class,	int.class,	charImpl);
		registerOperator(registry, operator, byte.class, 	long.class,	int.class,	byteImpl);
		registerOperator(registry, operator, short.class, 	long.class,	int.class,	shortImpl);
		registerOperator(registry, operator, int.class, 	long.class,	int.class,	intImpl);
		registerOperator(registry, operator, long.class,	long.class,	long.class,	longImpl);
	}

	private static void registerComparisonOperator(BinaryOperatorRegistry registry, String operator, BiFunction<Character, Character, Boolean> charImpl, BiFunction<Byte, Byte, Boolean> byteImpl, BiFunction<Short, Short, Boolean> shortImpl, BiFunction<Integer, Integer, Boolean> intImpl, BiFunction<Long, Long, Boolean> longImpl, BiFunction<Float, Float, Boolean> floatImpl, BiFunction<Double, Double, Boolean> doubleImpl) {
		registerComparisonOperator(registry, operator,	char.class,		charImpl);
		registerComparisonOperator(registry, operator,	byte.class,		byteImpl);
		registerComparisonOperator(registry, operator,	short.class,	shortImpl);
		registerComparisonOperator(registry, operator,	int.class,		intImpl);
		registerComparisonOperator(registry, operator,	long.class,		longImpl);
		registerComparisonOperator(registry, operator,	float.class,	floatImpl);
		registerComparisonOperator(registry, operator,	double.class,	doubleImpl);
	}

	private static void registerComparisonOperator(BinaryOperatorRegistry registry, String operator, BiFunction<Boolean, Boolean, Boolean> booleanImpl, BiFunction<Character, Character, Boolean> charImpl, BiFunction<Byte, Byte, Boolean> byteImpl, BiFunction<Short, Short, Boolean> shortImpl, BiFunction<Integer, Integer, Boolean> intImpl, BiFunction<Long, Long, Boolean> longImpl, BiFunction<Float, Float, Boolean> floatImpl, BiFunction<Double, Double, Boolean> doubleImpl) {
		registerComparisonOperator(registry, operator,	boolean.class,	booleanImpl);
		registerComparisonOperator(registry, operator,	char.class,		charImpl);
		registerComparisonOperator(registry, operator,	byte.class,		byteImpl);
		registerComparisonOperator(registry, operator,	short.class,	shortImpl);
		registerComparisonOperator(registry, operator,	int.class,		intImpl);
		registerComparisonOperator(registry, operator,	long.class,		longImpl);
		registerComparisonOperator(registry, operator,	float.class,	floatImpl);
		registerComparisonOperator(registry, operator,	double.class,	doubleImpl);
	}

	private static void registerBitOperator(BinaryOperatorRegistry registry, String operator, BiFunction<Boolean, Boolean, Boolean> booleanBooleanImpl, BiFunction<Character, Integer, Integer> charIntImpl, BiFunction<Byte, Integer, Integer> byteIntImpl, BiFunction<Short, Integer, Integer> shortIntImpl, BiFunction<Integer, Integer, Integer> intIntImpl, BiFunction<Long, Long, Long> longLongImpl) {
		registerOperator(registry, operator, boolean.class, 	boolean.class,		boolean.class,	booleanBooleanImpl);
		registerOperator(registry, operator, char.class, 		int.class,			int.class,	charIntImpl);
		registerOperator(registry, operator, byte.class, 		int.class,			int.class,	byteIntImpl);
		registerOperator(registry, operator, short.class, 		int.class,			int.class,	shortIntImpl);
		registerOperator(registry, operator, int.class, 		int.class,			int.class,	intIntImpl);
		registerOperator(registry, operator, long.class,		long.class,			long.class,	longLongImpl);
	}

	private static <T> void registerOperatorWithAssignment(BinaryOperatorRegistry registry, String operator, Class<T> lhsClass, BiFunction<T, Character, ?> charImpl, BiFunction<T, Byte, ?> byteImpl, BiFunction<T, Short, ?> shortImpl, BiFunction<T, Integer, ?> intImpl, BiFunction<T, Long, ?> longImpl, BiFunction<T, Float, ?> floatImpl, BiFunction<T, Double, ?> doubleImpl) {
		registerOperatorWithAssignment(registry, operator, lhsClass, charImpl, byteImpl, shortImpl, intImpl, longImpl);

		registerOperator(registry, operator, lhsClass, float.class,		lhsClass, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> lhsClass.cast(floatImpl.apply(a, b)));
		registerOperator(registry, operator, lhsClass, double.class,	lhsClass, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> lhsClass.cast(doubleImpl.apply(a, b)));
	}

	private static <T> void registerOperatorWithAssignment(BinaryOperatorRegistry registry, String operator, Class<T> lhsClass, BiFunction<T, Character, ?> charImpl, BiFunction<T, Byte, ?> byteImpl, BiFunction<T, Short, ?> shortImpl, BiFunction<T, Integer, ?> intImpl, BiFunction<T, Long, ?> longImpl) {
		registerOperator(registry, operator, lhsClass, char.class,		lhsClass, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> lhsClass.cast(charImpl.apply(a, b)));
		registerOperator(registry, operator, lhsClass, byte.class,		lhsClass, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> lhsClass.cast(byteImpl.apply(a, b)));
		registerOperator(registry, operator, lhsClass, short.class,		lhsClass, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> lhsClass.cast(shortImpl.apply(a, b)));
		registerOperator(registry, operator, lhsClass, int.class,		lhsClass, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> lhsClass.cast(intImpl.apply(a, b)));
		registerOperator(registry, operator, lhsClass, long.class,		lhsClass, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> lhsClass.cast(longImpl.apply(a, b)));
	}

	private static <O> void registerComparisonOperator(BinaryOperatorRegistry registry, String operator, Class<O> operandClass, BiFunction<O, O, Boolean> implementation) {
		registerOperator(registry, operator, operandClass, boolean.class, implementation);
	}

	private static <O, RESULT> void registerOperator(BinaryOperatorRegistry registry, String operator, Class<O> operandClass, Class<RESULT> resultClass, BiFunction<O, O, RESULT> implementation) {
		registerOperator(registry, operator, operandClass, operandClass, resultClass, implementation);
	}

	private static <L, R, RESULT> void registerOperator(BinaryOperatorRegistry registry, String operator, Class<L> lhsOperandClass, Class<R> rhsOperandClass, Class<RESULT> resultClass, BiFunction<L, R, RESULT> implementation) {
		registry.register(operator, lhsOperandClass, rhsOperandClass, resultClass, implementation);
	}

	private static <L, R, RESULT> void registerOperator(BinaryOperatorRegistry registry, String operator, Class<L> lhsOperandClass, Class<R> rhsOperandClass, Class<RESULT> resultClass, BinaryOperatorMode operatorMode, BiFunction<L, R, RESULT> implementation) {
		registry.register(operator, lhsOperandClass, rhsOperandClass, resultClass, operatorMode, implementation);
	}

	private static <L, R, RESULT> void registerOperator(BinaryOperatorRegistry registry, String operator, Class<L> lhsOperandClass, Class<R> rhsOperandClass, Class<RESULT> resultClass, BinaryOperatorMode operatorMode, BiFunction<L, R, RESULT> implementation, Function<L, NullableOptional<RESULT>> shortCircuitImplementation) {
		registry.register(operator, lhsOperandClass, rhsOperandClass, resultClass, operatorMode, implementation, shortCircuitImplementation);
	}

	private static <L, R, RESULT> void registerOperator(BinaryOperatorRegistry registry, String operator, Class<L> lhsOperandClass, Class<R> rhsOperandClass, BinaryOperatorMode operatorMode, BiFunction<L, R, RESULT> implementation, Function<L, NullableOptional<RESULT>> shortCircuitImplementation, BiFunction<Class<? extends L>, Class<? extends R>, Class<? extends RESULT>> resultClassProvider) {
		registry.register(operator, lhsOperandClass, rhsOperandClass, operatorMode, implementation, shortCircuitImplementation, resultClassProvider);
	}
}
