package dd.kms.zenodot.utils;

import dd.kms.zenodot.common.ReflectionUtils;
import dd.kms.zenodot.common.RegexUtils;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.parsers.AbstractEntityParser;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParseUtils
{
	/*
	 * Parsing
	 */
	public static <C> ParseResultIF parse(TokenStream tokenStream, C context, ParseExpectation expectation, AbstractEntityParser<? super C>... parsers) {
		List<ParseResultIF> parseResults = Arrays.stream(parsers)
			.map(parser -> parser.parse(tokenStream, context, expectation))
			.collect(Collectors.toList());
		return mergeParseResults(parseResults);
	}

	private static ParseResultIF mergeParseResults(List<ParseResultIF> parseResults) {
		if (parseResults.isEmpty()) {
			throw new IllegalArgumentException("Internal error: Cannot merge 0 parse results");
		}

		List<AmbiguousParseResult> ambiguousResults = filterParseResults(parseResults, AmbiguousParseResult.class);
		List<CompletionSuggestions> completionSuggestions = filterParseResults(parseResults, CompletionSuggestions.class);
		List<ParseError> errors = filterParseResults(parseResults, ParseError.class);
		List<ParseResultIF> results = new ArrayList<>();
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

	private static <T> List<T> filterParseResults(List<ParseResultIF> parseResults, Class<T> filterClass) {
		return parseResults.stream().filter(filterClass::isInstance).map(filterClass::cast).collect(Collectors.toList());
	}

	private static ParseResultIF mergeCompletionSuggestions(List<CompletionSuggestions> completionSuggestions) {
		Map<CompletionSuggestionIF, MatchRating> mergedRatedSuggestions = new LinkedHashMap<>();
		int position = Integer.MAX_VALUE;
		for (CompletionSuggestions suggestions : completionSuggestions) {
			position = Math.min(position, suggestions.getPosition());
			Map<CompletionSuggestionIF, MatchRating> ratedSuggestions = suggestions.getRatedSuggestions();
			for (CompletionSuggestionIF suggestion : ratedSuggestions.keySet()) {
				MatchRating rating = mergedRatedSuggestions.containsKey(suggestion)
										? mergedRatedSuggestions.get(suggestion)
										: MatchRating.NONE;
				MatchRating newRating = ratedSuggestions.get(suggestion);
				mergedRatedSuggestions.put(suggestion, MatchRatings.bestOf(rating, newRating));
			}
		}
		return new CompletionSuggestions(position, mergedRatedSuggestions);
	}

	private static ParseResultIF mergeResults(List<AmbiguousParseResult> ambiguousResults, List<ParseResultIF> results) {
		int position = ambiguousResults.isEmpty() ? results.get(0).getPosition() : ambiguousResults.get(0).getPosition();
		StringBuilder builder = new StringBuilder("Ambiguous expression:");
		for (AmbiguousParseResult ambiguousResult : ambiguousResults) {
			builder.append("\n").append(ambiguousResult.getMessage());
		}
		for (ParseResultIF result : results) {
			if (result instanceof ObjectParseResult) {
				builder.append("Expression can be evaluated to object of type ").append(((ObjectParseResult) result).getObjectInfo().getDeclaredType());
			} else if (result instanceof ClassParseResult) {
				builder.append("Expression can be evaluated to type ").append(((ClassParseResult) result).getType());
			} else {
				throw new IllegalArgumentException("Internal error: Expected an object or a class as parse result, but found " + result.getClass().getSimpleName());
			}
		}
		return new AmbiguousParseResult(position, builder.toString());
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
			/*
			 * Heuristic: Only consider errors with maximum position. These are probably
			 *            errors of parsers that are most likely supposed to match.
			 */
			int maxPosition = errorsOfCurrentType.stream().mapToInt(ParseError::getPosition).max().getAsInt();
			String message = errorsOfCurrentType.stream()
								.filter(error -> error.getPosition() == maxPosition)
								.map(ParseError::getMessage)
								.collect(Collectors.joining("\n"));
			return new ParseError(maxPosition, message, errorType);
		}
		return new ParseError(-1, "Internal error: Failed merging parse errors", ParseError.ErrorType.INTERNAL_ERROR);
	}

	/*
	 * Completion Suggestions
	 */
	public static <T> Map<CompletionSuggestionIF, MatchRating> createRatedSuggestions(Collection<T> objects, Function<T, CompletionSuggestionIF> suggestionBuilder, Function<T, MatchRating> ratingFunc) {
		Map<CompletionSuggestionIF, MatchRating> ratedSuggestions = new LinkedHashMap<>();
		for (T object : objects) {
			CompletionSuggestionIF suggestion = suggestionBuilder.apply(object);
			MatchRating rating = ratingFunc.apply(object);
			ratedSuggestions.put(suggestion, rating);
		}
		return ratedSuggestions;
	}

	/**
	 * Throws an IllegalArgumentException if the parse result is an object parse result when a class parse
	 * result is expected or vice versa.
	 *
	 * Returns true if the parse result is not of the expected type. This is the case for completion suggestions,
	 * errors, and ambiguous parse results.
	 */
	public static boolean propagateParseResult(ParseResultIF parseResult, ParseExpectation expectation) {
		ParseResultType parseResultType = parseResult.getResultType();
		ParseResultType expectedEvaluationType = expectation.getEvaluationType();
		if (expectedEvaluationType == ParseResultType.OBJECT_PARSE_RESULT && parseResultType == ParseResultType.CLASS_PARSE_RESULT) {
			throw new IllegalStateException("Internal error: Expected an object as parse result, but obtained a class");
		}
		if (expectedEvaluationType == ParseResultType.CLASS_PARSE_RESULT && parseResultType == ParseResultType.OBJECT_PARSE_RESULT) {
			throw new IllegalStateException("Internal error: Expected a class as parse result, but obtained an object");
		}
		return parseResultType != expectedEvaluationType;
	}
}
