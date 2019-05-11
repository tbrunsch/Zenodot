package dd.kms.zenodot.utils;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.parsers.AbstractEntityParser;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.tokenizer.TokenStream;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collection of utility methods for parsers
 */
public class ParseUtils
{
	/*
	 * Parsing
	 */

	/**
	 * Tries to parse a subexpression with the specified parsers. The result is obtained from merging the result
	 * of each of the specified parsers.
	 */
	public static <C> ParseResult parse(TokenStream tokenStream, C context, ParseExpectation expectation, AbstractEntityParser<? super C>... parsers) {
		List<ParseResult> parseResults = Arrays.stream(parsers)
			.map(parser -> parser.parse(tokenStream, context, expectation))
			.collect(Collectors.toList());
		return mergeParseResults(parseResults);
	}

	private static ParseResult mergeParseResults(List<ParseResult> parseResults) {
		if (parseResults.isEmpty()) {
			throw new IllegalArgumentException("Internal error: Cannot merge 0 parse results");
		}

		List<AmbiguousParseResult> ambiguousResults = filterParseResults(parseResults, AmbiguousParseResult.class);
		List<CompletionSuggestions> completionSuggestions = filterParseResults(parseResults, CompletionSuggestions.class);
		List<ParseError> errors = filterParseResults(parseResults, ParseError.class);
		List<ParseResult> results = new ArrayList<>();
		results.addAll(filterParseResults(parseResults, ObjectParseResult.class));
		results.addAll(filterParseResults(parseResults, ClassParseResult.class));

		if (!completionSuggestions.isEmpty()) {
			return mergeCompletionSuggestions(completionSuggestions);
		}

		boolean ambiguous = !ambiguousResults.isEmpty() || results.size() > 1;
		if (ambiguous) {
			return mergeResults(ambiguousResults, results);
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

	private static <T> List<T> filterParseResults(List<ParseResult> parseResults, Class<T> filterClass) {
		return parseResults.stream().filter(filterClass::isInstance).map(filterClass::cast).collect(Collectors.toList());
	}

	private static ParseResult mergeCompletionSuggestions(List<CompletionSuggestions> completionSuggestions) {
		Map<CompletionSuggestion, MatchRating> mergedRatedSuggestions = new LinkedHashMap<>();
		int position = Integer.MAX_VALUE;
		Optional<ExecutableArgumentInfo> methodArgumentInfo	= Optional.empty();
		for (CompletionSuggestions suggestions : completionSuggestions) {
			position = Math.min(position, suggestions.getPosition());
			Map<CompletionSuggestion, MatchRating> ratedSuggestions = suggestions.getRatedSuggestions();
			for (CompletionSuggestion suggestion : ratedSuggestions.keySet()) {
				MatchRating rating = mergedRatedSuggestions.containsKey(suggestion)
										? mergedRatedSuggestions.get(suggestion)
										: MatchRatings.NONE;
				MatchRating newRating = ratedSuggestions.get(suggestion);
				mergedRatedSuggestions.put(suggestion, MatchRatings.bestOf(rating, newRating));
			}
			if (!methodArgumentInfo.isPresent()) {
				methodArgumentInfo = suggestions.getExecutableArgumentInfo();
			}
		}
		return new CompletionSuggestions(position, mergedRatedSuggestions, methodArgumentInfo);
	}

	private static ParseResult mergeResults(List<AmbiguousParseResult> ambiguousResults, List<ParseResult> results) {
		int position = ambiguousResults.isEmpty() ? results.get(0).getPosition() : ambiguousResults.get(0).getPosition();
		StringBuilder builder = new StringBuilder("Ambiguous expression:");
		for (AmbiguousParseResult ambiguousResult : ambiguousResults) {
			builder.append("\n").append(ambiguousResult.getMessage());
		}
		for (ParseResult result : results) {
			if (result instanceof ObjectParseResult) {
				builder.append("Expression can be evaluated to object of type ").append(((ObjectParseResult) result).getObjectInfo().getDeclaredType());
			} else if (result instanceof ClassParseResult) {
				builder.append("Expression can be evaluated to type ").append(((ClassParseResult) result).getType());
			} else {
				throw new IllegalArgumentException("Internal error: Expected an object or a class as parse result, but found " + result.getClass().getSimpleName());
			}
		}
		return ParseResults.createAmbiguousParseResult(position, builder.toString());
	}

	private static ParseError mergeParseErrors(List<ParseError> errors) {
		for (ErrorPriority errorType : ErrorPriority.values()) {
			List<ParseError> errorsOfCurrentType = errors.stream().filter(error -> error.getErrorType() == errorType).collect(Collectors.toList());
			if (errorsOfCurrentType.isEmpty()) {
				continue;
			}
			if (errorsOfCurrentType.size() == 1) {
				return errorsOfCurrentType.get(0);
			}
			/*
			 * Heuristic: Only consider errors with maximum position. These are probably
			 *            errors of parsers that are most likely supposed to match.
			 */
			int maxPosition = errorsOfCurrentType.stream().mapToInt(ParseError::getPosition).max().getAsInt();
			final String message;
			if (errorType == ErrorPriority.WRONG_PARSER) {
				message = "Invalid expression";
			} else {
				message = errorsOfCurrentType.stream()
					.filter(error -> error.getPosition() == maxPosition)
					.map(ParseError::getMessage)
					.distinct()
					.collect(Collectors.joining("\n"));
			}

			Optional<Throwable> firstException = errorsOfCurrentType.stream()
				.map(ParseError::getThrowable)
				.filter(Objects::nonNull)
				.findFirst();

			return ParseResults.createParseError(maxPosition, message, errorType, firstException.orElse(null));
		}
		return ParseResults.createParseError(-1, "Internal error: Failed merging parse errors", ErrorPriority.INTERNAL_ERROR);
	}

	/*
	 * Completion Suggestions
	 */
	public static <T> Map<CompletionSuggestion, MatchRating> createRatedSuggestions(Iterable<? extends T> objects, Function<T, CompletionSuggestion> suggestionBuilder, Function<T, MatchRating> ratingFunc) {
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = new LinkedHashMap<>();
		for (T object : objects) {
			CompletionSuggestion suggestion = suggestionBuilder.apply(object);
			MatchRating rating = ratingFunc.apply(object);
			ratedSuggestions.put(suggestion, rating);
		}
		return ratedSuggestions;
	}

	/**
	 * Throws an IllegalArgumentException if the parse result is an object parse result when a class parse
	 * result is expected or vice versa.
	 *
	 * Returns a non-empty {@link ParseResult} if the parse result is not of the expected type. This is the
	 * case for completion suggestions, errors, and ambiguous parse results. In this case, the parse result
	 * shall be propagated immediately.
	 *
	 * In case of an error, the {@link ErrorPriority} is increased (i.e., given a higher priority) to
	 * the specified level if its current error type has lower priority.
	 *
	 * If the parse result matches the expectations, then it shall not be propagated and the parser has to
	 * continue parsing the expression. In that case, the returned {@link Optional} is empty.
	 */
	public static Optional<ParseResult> prepareParseResultForPropagation(ParseResult parseResult, ParseExpectation expectation, ErrorPriority minimumErrorType) {
		ParseResultType parseResultType = parseResult.getResultType();
		ParseResultType expectedEvaluationType = expectation.getEvaluationType();
		if (expectedEvaluationType == ParseResultType.OBJECT_PARSE_RESULT && parseResultType == ParseResultType.CLASS_PARSE_RESULT) {
			throw new IllegalStateException("Internal error: Expected an object as parse result, but obtained a class");
		}
		if (expectedEvaluationType == ParseResultType.CLASS_PARSE_RESULT && parseResultType == ParseResultType.OBJECT_PARSE_RESULT) {
			throw new IllegalStateException("Internal error: Expected a class as parse result, but obtained an object");
		}
		if (parseResultType == expectedEvaluationType) {
			return Optional.empty();
		}
		if (parseResultType != ParseResultType.PARSE_ERROR) {
			return Optional.of(parseResult);
		}
		ParseError parseError = (ParseError) parseResult;
		if (parseError.getErrorType().compareTo(minimumErrorType) <= 0) {
			// error has already sufficient priority
			return Optional.of(parseError);
		}
		return Optional.of(ParseResults.createParseError(parseError.getPosition(), parseError.getMessage(), minimumErrorType, parseError.getThrowable()));
	}
}
