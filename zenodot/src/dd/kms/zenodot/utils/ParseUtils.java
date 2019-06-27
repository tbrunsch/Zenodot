package dd.kms.zenodot.utils;

import com.google.common.collect.Iterables;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.parsers.AbstractParser;
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
	public static ParseOutcome parseClass(TokenStream tokenStream, ParserToolbox parserToolbox) {
		ParseOutcome classParseOutcome = parse(tokenStream, null, ParseExpectation.CLASS,
			parserToolbox.getImportedClassParser(),
			parserToolbox.getRootpackageParser()
		);
		checkExpectedParseResultType(classParseOutcome, ParseExpectation.CLASS);
		return classParseOutcome;
	}

	/**
	 * Tries to parse a subexpression with the specified parsers. The result is obtained from merging the result
	 * of each of the specified parsers.
	 */
	public static <C> ParseOutcome parse(TokenStream tokenStream, C context, ParseExpectation expectation, AbstractParser<? super C>... parsers) {
		List<ParseOutcome> parseOutcomes = Arrays.stream(parsers)
			.map(parser -> parser.parse(tokenStream, context, expectation))
			.collect(Collectors.toList());
		return parseOutcomes.size() == 1 ? Iterables.getOnlyElement(parseOutcomes) : mergeParseOutcomes(parseOutcomes);
	}

	private static ParseOutcome mergeParseOutcomes(List<ParseOutcome> parseOutcomes) {
		if (parseOutcomes.isEmpty()) {
			throw new IllegalArgumentException("Internal error: Cannot merge 0 parse outcomes");
		}

		List<AmbiguousParseResult> ambiguousResults = filterParseOutcomes(parseOutcomes, AmbiguousParseResult.class);
		List<CompletionSuggestions> completionSuggestions = filterParseOutcomes(parseOutcomes, CompletionSuggestions.class);
		List<ParseError> errors = filterParseOutcomes(parseOutcomes, ParseError.class);
		List<ParseOutcome> results = new ArrayList<>();
		results.addAll(filterParseOutcomes(parseOutcomes, ObjectParseResult.class));
		results.addAll(filterParseOutcomes(parseOutcomes, ClassParseResult.class));

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

	private static <T> List<T> filterParseOutcomes(List<ParseOutcome> parseResults, Class<T> filterClass) {
		return parseResults.stream().filter(filterClass::isInstance).map(filterClass::cast).collect(Collectors.toList());
	}

	private static ParseOutcome mergeCompletionSuggestions(List<CompletionSuggestions> completionSuggestions) {
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

	private static ParseOutcome mergeResults(List<AmbiguousParseResult> ambiguousResults, List<ParseOutcome> results) {
		int position = ambiguousResults.isEmpty() ? results.get(0).getPosition() : ambiguousResults.get(0).getPosition();
		StringBuilder builder = new StringBuilder("Ambiguous expression:");
		for (AmbiguousParseResult ambiguousResult : ambiguousResults) {
			builder.append("\n").append(ambiguousResult.getMessage());
		}
		for (ParseOutcome result : results) {
			if (result instanceof ObjectParseResult) {
				builder.append("Expression can be evaluated to object of type ").append(((ObjectParseResult) result).getObjectInfo().getDeclaredType());
			} else if (result instanceof ClassParseResult) {
				builder.append("Expression can be evaluated to type ").append(((ClassParseResult) result).getType());
			} else if (result instanceof PackageParseResult) {
				builder.append("Expression can be evaluated to package ").append(((PackageParseResult) result).getPackage());
			} else {
				throw new IllegalArgumentException("Internal error: Expected an object or a class as parse result, but found " + result.getClass().getSimpleName());
			}
		}
		return ParseOutcomes.createAmbiguousParseResult(position, builder.toString());
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

			return ParseOutcomes.createParseError(maxPosition, message, errorType, firstException.orElse(null));
		}
		return ParseOutcomes.createParseError(-1, "Internal error: Failed merging parse errors", ErrorPriority.INTERNAL_ERROR);
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
	 * Throws an IllegalArgumentException if the parse outcome is an object parse result when a class parse
	 * result is expected or vice versa.
	 *
	 * Returns a non-empty {@link ParseOutcome} if the parse result is not of the expected type. This is the
	 * case for completion suggestions, errors, and ambiguous parse results. In this case, the parse result
	 * shall be propagated immediately.
	 *
	 * In case of an error, the {@link ErrorPriority} is increased (i.e., given a higher priority) to
	 * the specified level if its current error type has lower priority.
	 *
	 * If the parse result matches the expectations, then it shall not be propagated and the parser has to
	 * continue parsing the expression. In that case, the returned {@link Optional} is empty.
	 */
	public static Optional<ParseOutcome> prepareParseOutcomeForPropagation(ParseOutcome parseOutcome, ParseExpectation expectation, ErrorPriority minimumErrorType) {
		checkExpectedParseResultType(parseOutcome, expectation);
		ParseOutcomeType parseOutcomeType = parseOutcome.getOutcomeType();
		if (parseOutcomeType == ParseOutcomeType.RESULT) {
			// do not propagate valid result, but process it further
			return Optional.empty();
		}
		if (parseOutcomeType != ParseOutcomeType.ERROR) {
			// propagate non-errors as-is
			return Optional.of(parseOutcome);
		}
		// errors need a priority justification
		ParseError parseError = (ParseError) parseOutcome;
		if (parseError.getErrorType().compareTo(minimumErrorType) <= 0) {
			// error has already sufficient priority
			return Optional.of(parseError);
		}
		return Optional.of(ParseOutcomes.createParseError(parseError.getPosition(), parseError.getMessage(), minimumErrorType, parseError.getThrowable()));
	}

	public static void checkExpectedParseResultType(ParseOutcome parseOutcome, ParseExpectation expectation) {
		ParseResultType expectedResultType = expectation.getResultType();
		ParseOutcomeType parseOutcomeType = parseOutcome.getOutcomeType();
		if (parseOutcomeType != ParseOutcomeType.RESULT) {
			// only results have to be checked
			return;
		}
		ParseResult parseResult = (ParseResult) parseOutcome;
		ParseResultType resultType = parseResult.getResultType();
		if (resultType != expectedResultType) {
			throw new IllegalStateException("Internal error: Expected a parse result of type '" + expectedResultType + "', but obtained a parse result of type '" + resultType + "'");
		}
	}
}
