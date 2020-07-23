package dd.kms.zenodot.utils;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.AbstractParser;
import dd.kms.zenodot.parsers.ParserConfidence;
import dd.kms.zenodot.parsers.RootpackageParser;
import dd.kms.zenodot.parsers.UnqualifiedClassParser;
import dd.kms.zenodot.parsers.expectations.ClassParseResultExpectation;
import dd.kms.zenodot.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.tokenizer.CompletionInfo;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

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
	public static ClassParseResult parseClass(TokenStream tokenStream, ParserToolbox parserToolbox) throws InternalParseException, InternalErrorException, CodeCompletionException, AmbiguousParseResultException, InternalEvaluationException {
		List<AbstractParser<ObjectInfo, ClassParseResult, ClassParseResultExpectation>> parsers = Arrays.asList(
			parserToolbox.createParser(UnqualifiedClassParser.class),
			parserToolbox.createParser(RootpackageParser.class)
		);
		// possible ambiguities: imported class name identical to class name in default package => imported class wins
		return parse(tokenStream, null, new ClassParseResultExpectation(), parsers);
	}

	/**
	 * Tries to parse a subexpression with the specified parsers. The result is obtained from merging the result
	 * of each of the specified parsers.
	 */
	public static <C, T extends ParseResult, S extends ParseResultExpectation<T>> T parse(TokenStream tokenStream, C context, S expectation, List<? extends AbstractParser<? super C, T, S>> parsers) throws InternalParseException, InternalEvaluationException, AmbiguousParseResultException, InternalErrorException, CodeCompletionException {
		if (parsers.isEmpty()) {
			throw new InternalErrorException(tokenStream.toString() + ": No parsers specified");
		}
		InternalLogger logger = parsers.get(0).getLogger();
		logger.log(ParseUtils.class, LogLevel.INFO, "Merging results of multiple parsers...");
		ParseResultMerger<C, T, S> merger = new ParseResultMerger<>(tokenStream, context, expectation);
		try {
			for (AbstractParser<? super C, T, S> parser : parsers) {
				merger.parse(parser);
			}
			T mergedResult = merger.merge();
			logger.log(ParseUtils.class, LogLevel.SUCCESS, "obtained result");
			return mergedResult;
		} catch (CodeCompletionException e) {
			logger.log(ParseUtils.class, LogLevel.SUCCESS, "merged completions");
			throw e;
		} catch (InternalErrorException | AmbiguousParseResultException | InternalEvaluationException | InternalParseException e) {
			logger.log(ParseUtils.class, e, true);
			throw e;
		}
	}

	/*
	 * Exception Formatting
	 */
	public static StringBuilder formatException(Throwable t, StringBuilder builder) {
		String exceptionDescription = getExceptionDescription(t);
		builder.append(exceptionDescription);
		String message = t.getMessage();
		if (message != null) {
			builder.append(": ").append(message);
		}
		Throwable cause = t.getCause();
		if (cause != null) {
			builder.append("\n");
			formatException(cause, builder);
		}
		return builder;
	}

	private static String getExceptionDescription(Throwable t) {
		if (t instanceof CodeCompletionException) {
			return "Code completions";
		} else if (t instanceof InternalErrorException) {
			return "Internal error";
		} else if (t instanceof InternalParseException) {
			return "Parse exception";
		} else if (t instanceof InternalEvaluationException) {
			return "Evaluation exception";
		} else if (t instanceof AmbiguousParseResultException) {
			return "Ambiguous parse results";
		} else {
			return t.getClass().getSimpleName();
		}
	}

	/*
	 * Code Completions
	 */
	public static <T> List<CodeCompletion> createCodeCompletions(Iterable<? extends T> objects, Function<T, CodeCompletion> completionBuilder) {
		List<CodeCompletion> codeCompletions = new ArrayList<>();
		for (T object : objects) {
			CodeCompletion codeCompletion = completionBuilder.apply(object);
			codeCompletions.add(codeCompletion);
		}
		return codeCompletions;
	}

	private static class ParseState
	{
		private final int				position;
		private final ParserConfidence	confidence;

		private ParseState(int position, ParserConfidence confidence) {
			this.position = position;
			this.confidence = confidence;
		}

		int getPosition() {
			return position;
		}

		ParserConfidence getConfidence() {
			return confidence;
		}
	}

	private static class ParseResultMerger<C, T extends ParseResult, S extends ParseResultExpectation<T>>
	{
		private final TokenStream					tokenStream;
		private final int							position;
		private final C								context;
		private final S								expectation;

		/**
		 * May contain {@link InternalParseException}s and {@link CodeCompletionException}s, also mixed.
		 */
		private final Map<InternalParseException, ParseState> 	parseExceptions			= new HashMap<>();
		private final Map<CodeCompletionException, ParseState>	completionExceptions	= new HashMap<>();

		private T									mergedResult;
		private boolean								foundResult;

		private ParseResultMerger(TokenStream tokenStream, C context, S expectation) {
			this.tokenStream = tokenStream;
			this.position = tokenStream.getPosition();
			this.context = context;
			this.expectation = expectation;
		}

		void parse(AbstractParser<? super C, T, S> parser) throws InternalErrorException, AmbiguousParseResultException, InternalEvaluationException, InternalParseException {
			if (foundResult) {
				return;
			}
			try {
				mergedResult = parser.parse(tokenStream, context, expectation);
				foundResult = true;
				if (parser.getConfidence() != ParserConfidence.RIGHT_PARSER) {
					throw new InternalErrorException(parser.getClass().getSimpleName() + " returned result although it is not sure to be the right parser.");
				}
			} catch (InternalParseException e) {
				ParserConfidence confidence = parser.getConfidence();
				if (confidence == ParserConfidence.RIGHT_PARSER) {
					throw e;
				}
				ParseState state = new ParseState(tokenStream.getPosition(), confidence);
				parseExceptions.put(e, state);
				tokenStream.setPosition(position);
			} catch (CodeCompletionException e) {
				ParserConfidence confidence = parser.getConfidence();
				ParseState state = new ParseState(tokenStream.getPosition(), confidence);
				completionExceptions.put(e, state);
				tokenStream.setPosition(position);
			}
		}

		T merge() throws InternalParseException, CodeCompletionException, InternalErrorException {
			if (foundResult) {
				return mergedResult;
			}

			// Check code completions and results (must be mutual exclusive)
			if (!completionExceptions.isEmpty()) {
				setPositionFrom(completionExceptions.values());
				throw mergeCodeCompletions(completionExceptions.keySet());
			}

			// Check exceptions from potentially right parsers
			Map<InternalParseException, ParseState> potentiallyRightParseExceptions = filterParseExceptions(ParserConfidence.POTENTIALLY_RIGHT_PARSER);
			if (!potentiallyRightParseExceptions.isEmpty()) {
				setPositionFrom(potentiallyRightParseExceptions.values());
				throw mergeParseExceptions(potentiallyRightParseExceptions.keySet());
			}

			// Check other exceptions
			Map<InternalParseException, ParseState> wrongParseExceptions = filterParseExceptions(ParserConfidence.WRONG_PARSER);
			if (!wrongParseExceptions.isEmpty()) {
				setPositionFrom(wrongParseExceptions.values());
				throw mergeParseExceptions(wrongParseExceptions.keySet());
			}
			throw new InternalErrorException(tokenStream.toString() + ": Unexpected parse outcomes");
		}

		private CodeCompletionException mergeCodeCompletions(Collection<CodeCompletionException> completionExceptions) {
			if (completionExceptions.size() == 1) {
				return completionExceptions.iterator().next();
			}
			List<CodeCompletion> completions = new ArrayList<>();
			Optional<ExecutableArgumentInfo> methodArgumentInfo	= Optional.empty();
			for (CodeCompletionException codeCompletionException : completionExceptions) {
				CodeCompletions codeCompletions = codeCompletionException.getCompletions();
				if (!methodArgumentInfo.isPresent()) {
					methodArgumentInfo = codeCompletions.getExecutableArgumentInfo();
				}
				completions.addAll(codeCompletions.getCompletions());
			}
			CodeCompletions codeCompletions = new CodeCompletions(completions, methodArgumentInfo.orElse(null));
			return new CodeCompletionException(codeCompletions);
		}

		private static InternalParseException mergeParseExceptions(Collection<InternalParseException> parseExceptions) throws InternalErrorException {
			if (parseExceptions.isEmpty()) {
				throw new InternalErrorException("Trying to merge 0 parse exceptions");
			}
			if (parseExceptions.size() == 1) {
				return parseExceptions.iterator().next();
			}
			List<String> messages = new ArrayList<>();
			Throwable cause = null;
			for (InternalParseException parseException : parseExceptions) {
				String message = parseException.getMessage();
				if (cause == null) {
					cause = parseException.getCause();
				}
				messages.add(message);
			}
			String combinedMessage = String.join("\n", messages);
			return new InternalParseException(combinedMessage, cause);
		}

		private void setPositionFrom(Collection<ParseState> parseStates) throws InternalErrorException {
			if (parseStates.isEmpty()) {
				throw new InternalErrorException("Trying to set position from empty list of states");
			}
			int position = parseStates.stream().mapToInt(ParseState::getPosition).max().getAsInt();
			tokenStream.setPosition(position);
		}

		private Map<InternalParseException, ParseState> filterParseExceptions(ParserConfidence confidence) {
			return parseExceptions.entrySet().stream()
					.filter(entry -> entry.getValue().getConfidence() == confidence)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
	}
}
