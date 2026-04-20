package dd.kms.zenodotx.java.rule.operator.binary;

import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodotx.common.NullableOptional;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
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
		registerEqualityComparisonOperator(registry, "==", (a, b) -> a.booleanValue() == b.booleanValue(), (a, b) -> a.charValue() == b.charValue(), (a, b) -> a.byteValue() == b.byteValue(), (a, b) -> a.shortValue() == b.shortValue(), (a, b) -> a.intValue() == b.intValue(), (a, b) -> a.longValue() == b.longValue(), (a, b) -> a.floatValue() == b.floatValue(), (a, b) -> a.doubleValue() == b.doubleValue(), (a, b) -> a == b);
		registerEqualityComparisonOperator(registry, "!=", (a, b) -> a.booleanValue() != b.booleanValue(), (a, b) -> a.charValue() != b.charValue(), (a, b) -> a.byteValue() != b.byteValue(), (a, b) -> a.shortValue() != b.shortValue(), (a, b) -> a.intValue() != b.intValue(), (a, b) -> a.longValue() != b.longValue(), (a, b) -> a.floatValue() != b.floatValue(), (a, b) -> a.doubleValue() != b.doubleValue(), (a, b) -> a != b);
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
		registerOperator(registry, "=", Object.class, Object.class, (classA, classB) -> classB, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b, (lhsClass, rhsClass) -> !ReflectionUtils.isPrimitive(lhsClass));

		registerOperator(registry, "=", boolean.class, boolean.class, (classA, classB) -> classB, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b, (lhsClass, rhsClass) -> ReflectionUtils.isPrimitive(lhsClass));
		registerOperator(registry, "=", char.class, char.class, (classA, classB) -> classB, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b, (lhsClass, rhsClass) -> ReflectionUtils.isPrimitive(lhsClass));
		registerOperator(registry, "=", byte.class, byte.class, (classA, classB) -> classB, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b, (lhsClass, rhsClass) -> ReflectionUtils.isPrimitive(lhsClass));
		registerOperator(registry, "=", short.class, short.class, (classA, classB) -> classB, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b, (lhsClass, rhsClass) -> ReflectionUtils.isPrimitive(lhsClass));
		registerOperator(registry, "=", int.class, int.class, (classA, classB) -> classB, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b, (lhsClass, rhsClass) -> ReflectionUtils.isPrimitive(lhsClass));
		registerOperator(registry, "=", long.class, long.class, (classA, classB) -> classB, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b, (lhsClass, rhsClass) -> ReflectionUtils.isPrimitive(lhsClass));
		registerOperator(registry, "=", float.class, float.class, (classA, classB) -> classB, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b, (lhsClass, rhsClass) -> ReflectionUtils.isPrimitive(lhsClass));
		registerOperator(registry, "=", double.class, double.class, (classA, classB) -> classB, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> b, (lhsClass, rhsClass) -> ReflectionUtils.isPrimitive(lhsClass));

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

		// String concatenation
		registerOperator(registry, "+=", String.class, Object.class, String.class, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> a + b);
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

	private static void registerEqualityComparisonOperator(BinaryOperatorRegistry registry, String operator, BiFunction<Boolean, Boolean, Boolean> booleanImpl, BiFunction<Character, Character, Boolean> charImpl, BiFunction<Byte, Byte, Boolean> byteImpl, BiFunction<Short, Short, Boolean> shortImpl, BiFunction<Integer, Integer, Boolean> intImpl, BiFunction<Long, Long, Boolean> longImpl, BiFunction<Float, Float, Boolean> floatImpl, BiFunction<Double, Double, Boolean> doubleImpl, BiFunction<Object, Object, Boolean> objectImpl) {
		// Primitive comparisons can only be performed if one of the operand types is primitive
		registerComparisonOperator(registry, operator,	boolean.class,	booleanImpl,	(lhsType, rhsType) -> ReflectionUtils.isPrimitive(lhsType) || ReflectionUtils.isPrimitive(rhsType));
		registerComparisonOperator(registry, operator,	char.class,		charImpl,		(lhsType, rhsType) -> ReflectionUtils.isPrimitive(lhsType) || ReflectionUtils.isPrimitive(rhsType));
		registerComparisonOperator(registry, operator,	byte.class,		byteImpl,		(lhsType, rhsType) -> ReflectionUtils.isPrimitive(lhsType) || ReflectionUtils.isPrimitive(rhsType));
		registerComparisonOperator(registry, operator,	short.class,	shortImpl,		(lhsType, rhsType) -> ReflectionUtils.isPrimitive(lhsType) || ReflectionUtils.isPrimitive(rhsType));
		registerComparisonOperator(registry, operator,	int.class,		intImpl,		(lhsType, rhsType) -> ReflectionUtils.isPrimitive(lhsType) || ReflectionUtils.isPrimitive(rhsType));
		registerComparisonOperator(registry, operator,	long.class,		longImpl,		(lhsType, rhsType) -> ReflectionUtils.isPrimitive(lhsType) || ReflectionUtils.isPrimitive(rhsType));
		registerComparisonOperator(registry, operator,	float.class,	floatImpl,		(lhsType, rhsType) -> ReflectionUtils.isPrimitive(lhsType) || ReflectionUtils.isPrimitive(rhsType));
		registerComparisonOperator(registry, operator,	double.class,	doubleImpl,		(lhsType, rhsType) -> ReflectionUtils.isPrimitive(lhsType) || ReflectionUtils.isPrimitive(rhsType));

		/*
		 * Instance comparisons can only be performed if both operand types are not primitive. Additionally,
		 * if both operand types are not the "type of null", then one operand type must be convertible to
		 * the other operand type.
		 */
		registerComparisonOperator(registry, operator,	Object.class,	objectImpl, (lhsType, rhsType) -> {
			if (ReflectionUtils.isPrimitive(lhsType) || ReflectionUtils.isPrimitive(rhsType)) {
				return false;
			} else if (lhsType == InfoProvider.NO_TYPE || rhsType == InfoProvider.NO_TYPE) {
				return true;
			} else {
				return lhsType.isAssignableFrom(rhsType) || rhsType.isAssignableFrom(lhsType);
			}			
		});
	}

	private static void registerBitOperator(BinaryOperatorRegistry registry, String operator, BiFunction<Boolean, Boolean, Boolean> booleanBooleanImpl, BiFunction<Character, Integer, Integer> charIntImpl, BiFunction<Byte, Integer, Integer> byteIntImpl, BiFunction<Short, Integer, Integer> shortIntImpl, BiFunction<Integer, Integer, Integer> intIntImpl, BiFunction<Long, Long, Long> longLongImpl) {
		registerOperator(registry, operator, boolean.class, 	boolean.class,		boolean.class,	booleanBooleanImpl);
		registerOperator(registry, operator, char.class, 		int.class,			int.class,	charIntImpl);
		registerOperator(registry, operator, byte.class, 		int.class,			int.class,	byteIntImpl);
		registerOperator(registry, operator, short.class, 		int.class,			int.class,	shortIntImpl);
		registerOperator(registry, operator, int.class, 		int.class,			int.class,	intIntImpl);
		registerOperator(registry, operator, long.class,		long.class,			long.class,	longLongImpl);
	}

	private static <T> void registerOperatorWithAssignment(BinaryOperatorRegistry registry, String operator, Class<T> lhsType, BiFunction<T, Character, ?> charImpl, BiFunction<T, Byte, ?> byteImpl, BiFunction<T, Short, ?> shortImpl, BiFunction<T, Integer, ?> intImpl, BiFunction<T, Long, ?> longImpl, BiFunction<T, Float, ?> floatImpl, BiFunction<T, Double, ?> doubleImpl) {
		registerOperatorWithAssignment(registry, operator, lhsType, charImpl, byteImpl, shortImpl, intImpl, longImpl);

		registerOperator(registry, operator, lhsType, float.class,		lhsType, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> ReflectionUtils.convertTo(floatImpl.apply(a, b), lhsType, true));
		registerOperator(registry, operator, lhsType, double.class,	lhsType, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> ReflectionUtils.convertTo(doubleImpl.apply(a, b), lhsType, true));
	}

	private static <T> void registerOperatorWithAssignment(BinaryOperatorRegistry registry, String operator, Class<T> lhsType, BiFunction<T, Character, ?> charImpl, BiFunction<T, Byte, ?> byteImpl, BiFunction<T, Short, ?> shortImpl, BiFunction<T, Integer, ?> intImpl, BiFunction<T, Long, ?> longImpl) {
		registerOperator(registry, operator, lhsType, char.class,		lhsType, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> ReflectionUtils.convertTo(charImpl.apply(a, b), lhsType, true));
		registerOperator(registry, operator, lhsType, byte.class,		lhsType, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> ReflectionUtils.convertTo(byteImpl.apply(a, b), lhsType, true));
		registerOperator(registry, operator, lhsType, short.class,		lhsType, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> ReflectionUtils.convertTo(shortImpl.apply(a, b), lhsType, true));
		registerOperator(registry, operator, lhsType, int.class,		lhsType, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> ReflectionUtils.convertTo(intImpl.apply(a, b), lhsType, true));
		registerOperator(registry, operator, lhsType, long.class,		lhsType, BinaryOperatorMode.RETURN_RESULT_ASSIGN_RESULT_LEFT, (a, b) -> ReflectionUtils.convertTo(longImpl.apply(a, b), lhsType, true));
	}

	private static <O> void registerComparisonOperator(BinaryOperatorRegistry registry, String operator, Class<O> operandType, BiFunction<O, O, Boolean> implementation) {
		registerOperator(registry, operator, operandType, boolean.class, implementation);
	}

	private static <O> void registerComparisonOperator(BinaryOperatorRegistry registry, String operator, Class<O> operandType, BiFunction<O, O, Boolean> implementation, BiPredicate<Class<? extends O>, Class<? extends O>> applicableToOperandTypesPredicate) {
		BinaryOperatorInfo operatorInfo = new BinaryOperatorInfoBuilder<>(operator, operandType, operandType, boolean.class, implementation)
			.restrictApplicability(applicableToOperandTypesPredicate)
			.build();
		registry.register(operatorInfo);
	}

	private static <O, RESULT> void registerOperator(BinaryOperatorRegistry registry, String operator, Class<O> operandType, Class<RESULT> resultType, BiFunction<O, O, RESULT> implementation) {
		registerOperator(registry, operator, operandType, operandType, resultType, implementation);
	}

	private static <L, R, RESULT> void registerOperator(BinaryOperatorRegistry registry, String operator, Class<L> lhsOperandType, Class<R> rhsOperandType, Class<RESULT> resultType, BiFunction<L, R, RESULT> implementation) {
		BinaryOperatorInfo operatorInfo = new BinaryOperatorInfoBuilder<>(operator, lhsOperandType, rhsOperandType, resultType, implementation)
			.build();
		registry.register(operatorInfo);
	}

	private static <L, R, RESULT> void registerOperator(BinaryOperatorRegistry registry, String operator, Class<L> lhsOperandType, Class<R> rhsOperandType, Class<RESULT> resultType, BinaryOperatorMode operatorMode, BiFunction<L, R, RESULT> implementation) {
		BinaryOperatorInfo operatorInfo = new BinaryOperatorInfoBuilder<>(operator, lhsOperandType, rhsOperandType, resultType, implementation)
			.operatorMode(operatorMode)
			.build();
		registry.register(operatorInfo);
	}

	private static <L, R, RESULT> void registerOperator(BinaryOperatorRegistry registry, String operator, Class<L> lhsOperandType, Class<R> rhsOperandType, Class<RESULT> resultType, BinaryOperatorMode operatorMode, BiFunction<L, R, RESULT> implementation, Function<L, NullableOptional<RESULT>> shortCircuitImplementation) {
		BinaryOperatorInfo operatorInfo = new BinaryOperatorInfoBuilder<>(operator, lhsOperandType, rhsOperandType, resultType, implementation)
			.operatorMode(operatorMode)
			.shortCircuitImplementation(shortCircuitImplementation)
			.build();
		registry.register(operatorInfo);
	}

	private static <L, R, RESULT> void registerOperator(BinaryOperatorRegistry registry, String operator, Class<L> lhsOperandType, Class<R> rhsOperandType, BiFunction<Class<? extends L>, Class<? extends R>, Class<? extends RESULT>> resultTypeProvider, BinaryOperatorMode operatorMode, BiFunction<L, R, RESULT> implementation, BiPredicate<Class<? extends L>, Class<? extends R>> applicableToOperandTypesPredicate) {
		BinaryOperatorInfo operatorInfo = new BinaryOperatorInfoBuilder<>(operator, lhsOperandType, rhsOperandType, resultTypeProvider, implementation)
			.operatorMode(operatorMode)
			.restrictApplicability(applicableToOperandTypesPredicate)
			.build();
		registry.register(operatorInfo);
	}

	static String createOperandDescription(Class<?> operandType) {
		return operandType == InfoProvider.NO_TYPE ? "null" : "instances of type '" + operandType.getSimpleName() + "'";
	}
}
