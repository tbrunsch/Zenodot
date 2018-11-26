package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.parsers.AbstractJavaEntityParser;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.tokenizer.JavaTokenStream;
import com.google.common.base.Joiner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class ParseUtils
{
	/*
	 * Parsing
	 */
	public static ParseResultIF parse(final JavaTokenStream tokenStream, final ObjectInfo currentContextInfo, final List<Class<?>> expectedResultClasses, AbstractJavaEntityParser... parsers) {
		List<ParseResultIF> parseResults = Arrays.stream(parsers)
				.map(parser -> parser.parse(tokenStream, currentContextInfo, expectedResultClasses))
				.collect(Collectors.toList());
		return mergeParseResults(parseResults);
	}

	static ParseResultIF mergeParseResults(List<ParseResultIF> parseResults) {
		if (parseResults.isEmpty()) {
			throw new IllegalArgumentException("Cannot merge 0 parse results");
		}

		List<AmbiguousParseResult> ambiguousResults = filterParseResults(parseResults, AmbiguousParseResult.class);
		List<ParseResult> results = filterParseResults(parseResults, ParseResult.class);
		List<CompletionSuggestions> completionSuggestions = filterParseResults(parseResults, CompletionSuggestions.class);
		List<ParseError> errors = filterParseResults(parseResults, ParseError.class);

		if (!completionSuggestions.isEmpty()) {
			// Merge and return suggestions
			Map<CompletionSuggestionIF, Integer> mergedRatedSuggestions = new LinkedHashMap<>();
			for (CompletionSuggestions suggestions : completionSuggestions) {
				Map<CompletionSuggestionIF, Integer> ratedSuggestions = suggestions.getRatedSuggestions();
				for (CompletionSuggestionIF suggestion : ratedSuggestions.keySet()) {
					int currentRating = mergedRatedSuggestions.containsKey(suggestion)
							? mergedRatedSuggestions.get(suggestion)
							: Integer.MAX_VALUE;
					int newRating = ratedSuggestions.get(suggestion);
					int bestRating = Math.min(currentRating, newRating);
					mergedRatedSuggestions.put(suggestion, bestRating);
				}
			}
			return new CompletionSuggestions(mergedRatedSuggestions);
		}

		boolean ambiguous = !ambiguousResults.isEmpty() || results.size() > 1;
		if (ambiguous) {
			int position = ambiguousResults.isEmpty() ? results.get(0).getParsedToPosition() : ambiguousResults.get(0).getPosition();
			String message = "Ambiguous expression:\n"
					+ ambiguousResults.stream().map(AmbiguousParseResult::getMessage).collect(Collectors.joining("\n"))
					+ (ambiguousResults.size() > 0 && results.size() > 0 ? "\n" : "")
					+ results.stream().map(result -> "Expression can be evaluated to object of type " + result.getObjectInfo().getDeclaredClass().getSimpleName()).collect(Collectors.joining("\n"));
			return new AmbiguousParseResult(position, message);
		}

		if (results.size() == 1) {
			return results.get(0);
		}

		if (errors.size() > 1) {
			return mergeParseErrors(errors);
		} else {
			return errors.get(0);
		}
	}

	private static <T> List<T> filterParseResults(List<ParseResultIF> parseResults, Class<T> filterClass) {
		return parseResults.stream().filter(filterClass::isInstance).map(filterClass::cast).collect(Collectors.toList());
	}

	private static ParseError mergeParseErrors(List<ParseError> errors) {
		for (ParseError.ErrorType errorType : ParseError.ErrorType.values()) {
			List<ParseError> errorsOfCurrentType = errors.stream().filter(error -> error.getErrorType() == errorType).collect(Collectors.toList());
			if (errorsOfCurrentType.isEmpty()) {
				continue;
			}
			if (errorsOfCurrentType.size() == 1) {
				return errorsOfCurrentType.get(0);
			}
			Set<Integer> positions = errorsOfCurrentType.stream().map(ParseError::getPosition).collect(Collectors.toSet());
			final int position;
			final List<String> messages;
			final String messagePrefix;
			if (positions.size() == 1) {
				position = positions.iterator().next();
				messages = errorsOfCurrentType.stream().map(ParseError::getMessage).collect(Collectors.toList());
				messagePrefix = "";
			} else {
				position = 0;
				messages = errorsOfCurrentType.stream().map(error -> "Position " + error.getPosition() + ": " + error.getMessage()).collect(Collectors.toList());
				messagePrefix = "Could not parse expression due to the following errors: ";
			}
			String message = Joiner.on("\n").join(messages);
			return new ParseError(position, messagePrefix + message, errorType);
		}
		return new ParseError(-1, "Internal error: Failed merging parse errors", ParseError.ErrorType.INTERNAL_ERROR);
	}

	/*
	 * String Comparison
	 */
	private static final int STRING_MATCH_FULL							= 0;
	private static final int STRING_MATCH_FULL_IGNORE_CASE				= 1;
	private static final int STRING_MATCH_PREFIX						= 2;
	private static final int STRING_MATCH_PREFIX_IGNORE_CASE			= 3;
	private static final int STRING_MATCH_INVERSE_PREFIX				= 4;
	private static final int STRING_MATCH_INVERSE_PREFIX_IGNORE_CASE	= 5;
	private static final int STRING_MATCH_MIN_VALUE_OTHER				= 6;

	private static int rateStringMatch(String actual, String expected) {
		if (actual.equals(expected)) {
			return STRING_MATCH_FULL;
		} else {
			String actualLowerCase = actual.toLowerCase();
			String expectedLowerCase = expected.toLowerCase();
			if (actualLowerCase.equals(expectedLowerCase)) {
				return STRING_MATCH_FULL_IGNORE_CASE;
			} else if (actual.startsWith(expected)) {
				return STRING_MATCH_PREFIX;
			} else if (actualLowerCase.startsWith(expectedLowerCase)) {
				return STRING_MATCH_PREFIX_IGNORE_CASE;
			} else if (expected.startsWith(actual)) {
				return STRING_MATCH_INVERSE_PREFIX;
			} else if (expectedLowerCase.startsWith(actualLowerCase)) {
				return STRING_MATCH_INVERSE_PREFIX_IGNORE_CASE;
			} else {
				// TODO: Differentiate between Strings
				return STRING_MATCH_MIN_VALUE_OTHER;
			}
		}
	}

	static final int CLASS_MATCH_FULL					= 0;
	static final int CLASS_MATCH_INHERITANCE			= 1;
	static final int CLASS_MATCH_PRIMITIVE_CONVERSION	= 2;
	static final int CLASS_MATCH_BOXED					= 3;
	static final int CLASS_MATCH_BOXED_AND_CONVERSION	= 4;
	static final int CLASS_MATCH_BOXED_AND_INHERITANCE	= 5;
	static final int CLASS_MATCH_NONE					= 6;

	static int rateClassMatch(Class<?> actual, Class<?> expected) {
		if (expected == null) {
			// no expectations
			return CLASS_MATCH_FULL;
		}

		if (actual == null) {
			// null object (only object without class) is convertible to any non-primitive class
			return expected.isPrimitive() ? CLASS_MATCH_NONE : CLASS_MATCH_FULL;
		}

		if (actual == expected) {
			return CLASS_MATCH_FULL;
		}

		boolean primitiveConvertible = ReflectionUtils.isPrimitiveConvertibleTo(actual, expected, false);
		if (expected.isPrimitive()) {
			if (actual.isPrimitive()) {
				return primitiveConvertible
						? CLASS_MATCH_PRIMITIVE_CONVERSION	// int -> double
						: CLASS_MATCH_NONE;					// int -> boolean
			} else {
				Class<?> actualUnboxed = ReflectionUtils.getPrimitiveClass(actual);
				return	actualUnboxed == expected	? CLASS_MATCH_BOXED :				// Integer -> int
						primitiveConvertible		? CLASS_MATCH_BOXED_AND_CONVERSION	// Integer -> double
													: CLASS_MATCH_NONE;					// Integer -> boolean
			}
		} else {
			if (actual.isPrimitive()) {
				Class<?> actualBoxed = ReflectionUtils.getBoxedClass(actual);
				return	actualBoxed == expected					? CLASS_MATCH_BOXED :					// int -> Integer
						primitiveConvertible					? CLASS_MATCH_BOXED_AND_CONVERSION :	// int -> Double
						expected.isAssignableFrom(actualBoxed)	? CLASS_MATCH_BOXED_AND_INHERITANCE		// int -> Number
																: CLASS_MATCH_NONE;						// int -> String
			} else {
				return	expected.isAssignableFrom(actual)	? CLASS_MATCH_INHERITANCE	// Integer -> Number
															: CLASS_MATCH_NONE;			// Integer -> Double
			}
		}
	}

	public static boolean isConvertibleTo(Class<?> source, Class<?> target) {
		return rateClassMatch(source, target) != CLASS_MATCH_NONE;
	}

	/*
	 * Fields
	 */
	private static int rateFieldByName(Field field, String expectedFieldName) {
		return rateStringMatch(field.getName(), expectedFieldName);
	}

	static ToIntFunction<Field> rateFieldByClassesFunc(final List<Class<?>> expectedClasses) {
		return field -> rateFieldByClasses(field, expectedClasses);
	}

	private static int rateFieldByClasses(Field field, List<Class<?>> expectedClasses) {
		return	expectedClasses == null		? CLASS_MATCH_FULL :
				expectedClasses.isEmpty()	? CLASS_MATCH_NONE
											: expectedClasses.stream().mapToInt(expectedClass -> rateClassMatch(field.getType(), expectedClass)).min().getAsInt();
	}

	public static ToIntFunction<Field> rateFieldByNameAndClassesFunc(final String fieldName, final List<Class<?>> expectedClasses) {
		return field -> (CLASS_MATCH_NONE + 1)*rateFieldByName(field, fieldName) + rateFieldByClasses(field, expectedClasses);
	}

	public static String getFieldDisplayText(Field field) {
		return field.getName() + " (" + field.getDeclaringClass().getSimpleName() + ")";
	}

	/*
	 * Methods
	 */
	private static int rateMethodByName(Method method, String expectedMethodName) {
		return rateStringMatch(method.getName(), expectedMethodName);
	}

	static ToIntFunction<Method> rateMethodByClassesFunc(final List<Class<?>> expectedClasses) {
		return method -> rateMethodByClasses(method, expectedClasses);
	}

	private static int rateMethodByClasses(Method method, List<Class<?>> expectedClasses) {
		return	expectedClasses == null		? CLASS_MATCH_FULL :
				expectedClasses.isEmpty()	? CLASS_MATCH_NONE
											: expectedClasses.stream().mapToInt(expectedClass -> rateClassMatch(method.getReturnType(), expectedClass)).min().getAsInt();
	}

	public static ToIntFunction<Method> rateMethodByNameAndClassesFunc(final String methodName, final List<Class<?>> expectedClasses) {
		return method -> (CLASS_MATCH_NONE + 1)*rateMethodByName(method, methodName) + rateMethodByClasses(method, expectedClasses);
	}

	public static String getMethodDisplayText(Method method) {
		return method.getName() + " (" + method.getDeclaringClass().getSimpleName() + ")";
	}

	/*
	 * Classes
	 */
	private static int rateClassByName(JavaClassInfo classInfo, String expectedSimpleClassName) {
		// transformation required to make it comparable to rated fields and methods
		return (CLASS_MATCH_NONE + 1)*rateStringMatch(classInfo.getSimpleNameWithoutLeadingDigits(), expectedSimpleClassName) + CLASS_MATCH_NONE;
	}

	static ToIntFunction<JavaClassInfo> rateClassByNameFunc(final String simpleClassName) {
		return classInfo -> rateClassByName(classInfo, simpleClassName);
	}

	public static String getClassDisplayText(JavaClassInfo classInfo) {
		return classInfo.getName();
	}

	/*
	 * Packages
	 */
	private static int ratePackageByName(Package pack, String expectedPackageName) {
		String packageName = pack.getName();
		int lastDotIndex = packageName.lastIndexOf('.');
		String subpackageName = packageName.substring(lastDotIndex + 1);
		// transformation required to make it comparable to rated fields and methods
		return (CLASS_MATCH_NONE + 1)*rateStringMatch(subpackageName, expectedPackageName) + CLASS_MATCH_NONE;
	}

	static ToIntFunction<Package> ratePackageByNameFunc(final String packageName) {
		return pack -> ratePackageByName(pack, packageName);
	}

	public static String getPackageDisplayText(Package pack) {
		return pack.getName();
	}

	/*
	 * Completion Suggestions
	 */
	public static <T> Map<CompletionSuggestionIF, Integer> createRatedSuggestions(List<T> objects, Function<T, CompletionSuggestionIF> suggestionBuilder, ToIntFunction<T> ratingFunc) {
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = new LinkedHashMap<>();
		for (T object : objects) {
			CompletionSuggestionIF suggestion = suggestionBuilder.apply(object);
			int rating = ratingFunc.applyAsInt(object);
			ratedSuggestions.put(suggestion, rating);
		}
		return ratedSuggestions;
	}
}