package dd.kms.zenodot.impl.matching;

import com.google.common.primitives.Primitives;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.common.RegexUtils;
import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.impl.wrappers.InfoProvider;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Utility class for rating matches
 */
public class MatchRatings
{
	public static MatchRating create(StringMatch stringMatch, TypeMatch typeMatch, boolean accessDiscouraged) {
		return new MatchRatingImpl(stringMatch, typeMatch, accessDiscouraged);
	}

	/*
	 * String Comparison
	 */
	public static StringMatch rateStringMatch(String expected, String actual) {
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
			} else if (WildcardPatternGenerator.generate(expected).matcher(actual).matches()) {
				return StringMatch.WILDCARD;
			} else if (actualLowerCase.startsWith(expectedLowerCase)) {
				return StringMatch.PREFIX_IGNORE_CASE;
			} else if (expected.startsWith(actual)) {
				return StringMatch.INVERSE_PREFIX;
			} else if (expectedLowerCase.startsWith(actualLowerCase)) {
				return StringMatch.INVERSE_PREFIX_IGNORE_CASE;
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

	public static TypeMatch rateTypeMatch(Class<?> expectedClass, Class<?> actualClass) {
		if (expectedClass == InfoProvider.NO_TYPE) {
			// no expectations
			return TypeMatch.FULL;
		}

		if (actualClass == InfoProvider.NO_TYPE) {
			// null object (only object without class) is convertible to any non-primitive class
			return expectedClass.isPrimitive() ? TypeMatch.NONE : TypeMatch.FULL;
		}

		if (actualClass.equals(expectedClass)) {
			return TypeMatch.FULL;
		}

		if (actualClass == void.class && expectedClass != void.class) {
			return TypeMatch.NONE;
		}

		boolean primitiveConvertible = ReflectionUtils.isPrimitiveConvertibleTo(actualClass, expectedClass, false);
		if (expectedClass.isPrimitive()) {
			if (actualClass.isPrimitive()) {
				return primitiveConvertible
						? TypeMatch.PRIMITIVE_CONVERSION	// int -> double
						: TypeMatch.NONE;					// int -> boolean
			} else {
				Class<?> actualUnboxedClass = Primitives.unwrap(actualClass);
				return	actualUnboxedClass == expectedClass	? TypeMatch.BOXED :					// Integer -> int
						primitiveConvertible				? TypeMatch.BOXED_AND_CONVERSION	// Integer -> double
															: TypeMatch.NONE;					// Integer -> boolean
			}
		} else {
			if (actualClass.isPrimitive()) {
				Class<?> actualBoxedClass = Primitives.wrap(actualClass);
				return	actualBoxedClass == expectedClass					? TypeMatch.BOXED :					// int -> Integer
						primitiveConvertible								? TypeMatch.BOXED_AND_CONVERSION :	// int -> Double
						expectedClass.isAssignableFrom(actualBoxedClass)	? TypeMatch.BOXED_AND_INHERITANCE	// int -> Number
																			: TypeMatch.NONE;					// int -> String
			} else {
				return	expectedClass.isAssignableFrom(actualClass)	? TypeMatch.INHERITANCE		// ArrayList -> List
																	: TypeMatch.NONE;			// Integer -> Double
			}
		}
	}

	public static boolean isConvertibleTo(Class<?> source, Class<?> target) {
		return rateTypeMatch(target, source) != TypeMatch.NONE;
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
