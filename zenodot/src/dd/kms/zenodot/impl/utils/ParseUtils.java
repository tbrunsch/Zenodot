package dd.kms.zenodot.impl.utils;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.impl.flowcontrol.*;
import dd.kms.zenodot.impl.parsers.*;
import dd.kms.zenodot.impl.parsers.expectations.ClassParseResultExpectation;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.impl.result.ClassParseResult;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.impl.result.ObjectParseResult;
import dd.kms.zenodot.impl.result.ParseResult;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

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
	public static ClassParseResult parseClass(TokenStream tokenStream, ParserToolbox parserToolbox) throws SyntaxException, InternalErrorException, CodeCompletionException, EvaluationException {
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
	public static <C, T extends ParseResult, S extends ParseResultExpectation<T>> T parse(TokenStream tokenStream, C context, S expectation, List<? extends AbstractParser<? super C, T, S>> parsers) throws SyntaxException, EvaluationException, InternalErrorException, CodeCompletionException {
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
		} catch (InternalErrorException | EvaluationException | SyntaxException e) {
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
		} else if (t instanceof SyntaxException) {
			return "Syntax exception";
		} else if (t instanceof EvaluationException) {
			return "Evaluation exception";
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

	public static ObjectParseResult parseTail(TokenStream tokenStream, ParseResult parseResult, ParserToolbox parserToolbox, ObjectParseResultExpectation expectation) throws CodeCompletionException, SyntaxException, InternalErrorException, EvaluationException {
		if (parseResult instanceof ObjectParseResult) {
			ObjectParseResult objectParseResult = (ObjectParseResult) parseResult;
			ObjectInfo objectInfo = objectParseResult.getObjectInfo();
			AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation> objectTailParser = parserToolbox.createParser(ObjectTailParser.class);
			ObjectParseResult tailParseResult = objectTailParser.parse(tokenStream, objectInfo, expectation);
			return new ParseResultWithTail(objectParseResult, tailParseResult, tokenStream);
		} else if (parseResult instanceof ClassParseResult) {
			ClassParseResult classParseResult = (ClassParseResult) parseResult;
			Class<?> type = classParseResult.getType();
			AbstractParser<Class<?>, ObjectParseResult, ObjectParseResultExpectation> classTailParser = parserToolbox.createParser(ClassTailParser.class);
			return classTailParser.parse(tokenStream, type, expectation);
		} else {
			throw new InternalErrorException("Can only parse tails of objects and classes, but requested for " + parseResult.getClass().getSimpleName());
		}
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
		 * May contain {@link SyntaxException}s and {@link CodeCompletionException}s, also mixed.
		 */
		private final Map<SyntaxException, ParseState> 	parseExceptions			= new HashMap<>();
		private final Map<CodeCompletionException, ParseState>	completionExceptions	= new HashMap<>();

		private T									mergedResult;
		private boolean								foundResult;

		private ParseResultMerger(TokenStream tokenStream, C context, S expectation) {
			this.tokenStream = tokenStream;
			this.position = tokenStream.getPosition();
			this.context = context;
			this.expectation = expectation;
		}

		void parse(AbstractParser<? super C, T, S> parser) throws InternalErrorException, EvaluationException, SyntaxException {
			if (foundResult) {
				return;
			}
			try {
				mergedResult = parser.parse(tokenStream, context, expectation);
				foundResult = true;
				if (parser.getConfidence() != ParserConfidence.RIGHT_PARSER) {
					throw new InternalErrorException(parser.getClass().getSimpleName() + " returned result although it is not sure to be the right parser.");
				}
			} catch (SyntaxException e) {
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

		T merge() throws SyntaxException, CodeCompletionException, InternalErrorException {
			if (foundResult) {
				return mergedResult;
			}

			// Check code completions and results (must be mutual exclusive)
			if (!completionExceptions.isEmpty()) {
				throw mergeCodeCompletions(completionExceptions.keySet());
			}

			// Check exceptions from potentially right parsers
			Map<SyntaxException, ParseState> potentiallyRightParseExceptions = filterParseExceptions(ParserConfidence.POTENTIALLY_RIGHT_PARSER);
			if (!potentiallyRightParseExceptions.isEmpty()) {
				setPositionFrom(potentiallyRightParseExceptions.values());
				throw mergeParseExceptions(potentiallyRightParseExceptions.keySet());
			}

			// Check other exceptions
			Map<SyntaxException, ParseState> wrongParseExceptions = filterParseExceptions(ParserConfidence.WRONG_PARSER);
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

		private static SyntaxException mergeParseExceptions(Collection<SyntaxException> parseExceptions) throws InternalErrorException {
			if (parseExceptions.isEmpty()) {
				throw new InternalErrorException("Trying to merge 0 parse exceptions");
			}
			if (parseExceptions.size() == 1) {
				return parseExceptions.iterator().next();
			}
			List<String> messages = new ArrayList<>();
			Throwable cause = null;
			for (SyntaxException parseException : parseExceptions) {
				String message = parseException.getMessage();
				if (cause == null) {
					cause = parseException.getCause();
				}
				messages.add(message);
			}
			String combinedMessage = String.join("\n", messages);
			return new SyntaxException(combinedMessage, cause);
		}

		private void setPositionFrom(Collection<ParseState> parseStates) throws InternalErrorException {
			if (parseStates.isEmpty()) {
				throw new InternalErrorException("Trying to set position from empty list of states");
			}
			int position = parseStates.stream().mapToInt(ParseState::getPosition).max().getAsInt();
			tokenStream.setPosition(position);
		}

		private Map<SyntaxException, ParseState> filterParseExceptions(ParserConfidence confidence) {
			return parseExceptions.entrySet().stream()
					.filter(entry -> entry.getValue().getConfidence() == confidence)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
	}

	private static class ParseResultWithTail extends ObjectParseResult
	{
		private final ObjectParseResult parseResult;
		private final ObjectParseResult tailParseResult;

		ParseResultWithTail(ObjectParseResult parseResult, ObjectParseResult tailParseResult, TokenStream tokenStream) {
			super(tailParseResult.getObjectInfo(), tokenStream);
			this.parseResult = parseResult;
			this.tailParseResult = tailParseResult;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo context) throws ParseException {
			ObjectInfo nextObjectInfo = parseResult.evaluate(thisInfo, context);
			return tailParseResult.evaluate(thisInfo, nextObjectInfo);
		}
	}
}
