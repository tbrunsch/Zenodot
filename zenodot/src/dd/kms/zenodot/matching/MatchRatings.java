package dd.kms.zenodot.matching;

import dd.kms.zenodot.common.ReflectionUtils;
import dd.kms.zenodot.common.RegexUtils;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Objects;
import java.util.regex.Pattern;

public class MatchRatings
{
	/*
	 * String Comparison
	 */
	public static StringMatch rateStringMatch(String actual, String expected) {
		if (actual.equals(expected)) {
			return StringMatch.FULL;
		} else if (expected.isEmpty()) {
			return actual.isEmpty() ? StringMatch.FULL : StringMatch.PREFIX;
		} else {
			String actualLowerCase = actual.toLowerCase();
			String expectedLowerCase = expected.toLowerCase();
			if (actualLowerCase.equals(expectedLowerCase)) {
				return StringMatch.FULL_IGNORE_CASE;
			} else if (actual.startsWith(expected)) {
				return StringMatch.PREFIX;
			} else if (actualLowerCase.startsWith(expectedLowerCase)) {
				return StringMatch.PREFIX_IGNORE_CASE;
			} else if (expected.startsWith(actual)) {
				return StringMatch.INVERSE_PREFIX;
			} else if (expectedLowerCase.startsWith(actualLowerCase)) {
				return StringMatch.INVERSE_PREFIX_IGNORE_CASE;
			} else if (WildcardPatternGenerator.generate(expected).matcher(actual).matches()) {
				return StringMatch.WILDCARD;
			} else {
				return StringMatch.NONE;
			}
		}
	}

	/*
	 * Type Comparison
	 */
	public static TypeMatch worstOf(TypeMatch m1, TypeMatch m2) {
		return m1.compareTo(m2) <= 0 ? m2 : m1;
	}

	public static MatchRating bestOf(MatchRating m1, MatchRating m2) {
		return m1.compareTo(m2) <= 0 ? m1 : m2;
	}

	public static TypeMatch rateTypeMatch(TypeInfo actual, TypeInfo expected) {
		if (actual == TypeInfo.UNKNOWN) {
			throw new IllegalArgumentException("Internal error: Cannot rate type match for unknown type");
		}
		if (expected == TypeInfo.UNKNOWN) {
			throw new IllegalArgumentException("Internal error: Cannot expect unknown type");
		}

		if (expected == TypeInfo.NONE) {
			// no expectations
			return TypeMatch.FULL;
		}

		if (actual == TypeInfo.NONE) {
			// null object (only object without class) is convertible to any non-primitive class
			return expected.isPrimitive() ? TypeMatch.NONE : TypeMatch.FULL;
		}

		if (actual.equals(expected)) {
			return TypeMatch.FULL;
		}

		Class<?> actualClass = actual.getRawType();
		Class<?> expectedClass = expected.getRawType();
		boolean primitiveConvertible = ReflectionUtils.isPrimitiveConvertibleTo(actualClass, expectedClass, false);
		if (expected.isPrimitive()) {
			if (actual.isPrimitive()) {
				return primitiveConvertible
						? TypeMatch.PRIMITIVE_CONVERSION	// int -> double
						: TypeMatch.NONE;					// int -> boolean
			} else {
				Class<?> actualUnboxedClass = ReflectionUtils.getPrimitiveClass(actualClass);
				return	actualUnboxedClass == expectedClass	? TypeMatch.BOXED :					// Integer -> int
						primitiveConvertible				? TypeMatch.BOXED_AND_CONVERSION	// Integer -> double
															: TypeMatch.NONE;					// Integer -> boolean
			}
		} else {
			if (actual.isPrimitive()) {
				Class<?> actualBoxedClass = ReflectionUtils.getBoxedClass(actualClass);
				return	actualBoxedClass == expectedClass					? TypeMatch.BOXED :					// int -> Integer
						primitiveConvertible								? TypeMatch.BOXED_AND_CONVERSION :	// int -> Double
						expectedClass.isAssignableFrom(actualBoxedClass)	? TypeMatch.BOXED_AND_INHERITANCE 	// int -> Number
																			: TypeMatch.NONE;					// int -> String
			} else {
				return	expected.isSupertypeOf(actual)	? TypeMatch.INHERITANCE		// Integer -> Number
														: TypeMatch.NONE;			// Integer -> Double
			}
		}
	}

	public static boolean isConvertibleTo(TypeInfo source, TypeInfo target) {
		return rateTypeMatch(source, target) != TypeMatch.NONE;
	}

	private static class WildcardPatternGenerator
	{
		private static String	WILDCARD_STRING;	// caches last wildcard string
		private static Pattern	PATTERN;

		static Pattern generate(String wildcardString) {
			if (!Objects.equals(wildcardString, WILDCARD_STRING)) {
				WILDCARD_STRING = wildcardString;
				PATTERN = RegexUtils.createRegexForWildcardString(wildcardString);
			}
			return PATTERN;
		}
	}
}