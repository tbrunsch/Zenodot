package dd.kms.zenodot;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLoggers;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.Map;
import java.util.Optional;

import static dd.kms.zenodot.result.ParseError.ErrorPriority;

/**
 * API entry point of Zenodot<br/>
 * <br/>
 * Zenodot suggests completions and evaluates expressions in the context of a certain object referred to by {@code this}.
 * If the context refers to, e.g., a list, then you can simply type {@code size()} to get the size of that list. You do
 * not have to enter {@code this.size()}.
 */
public class JavaParser
{
	/**
	 * Returns rated code completions for a given java expression at a given caret position in the context provided
	 * by {@code thisValue}.
	 *
	 * @throws ParseException
	 */
	public static Map<CompletionSuggestion, MatchRating> suggestCodeCompletion(String javaExpression, int caretPosition, ParserSettings settings, Object thisValue) throws ParseException {
		return getCompletionSuggestions(javaExpression, caretPosition, settings, thisValue).getRatedSuggestions();
	}

	/**
	 * Returns optional information about the arguments of the current method or constructor {@link ExecutableArgumentInfo}.
	 * The value will be present if the caret is inside of a method argument list.
	 */
	public static Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String javaExpression, int caretPosition, ParserSettings settings, Object thisValue) throws ParseException {
		return getCompletionSuggestions(javaExpression, caretPosition, settings, thisValue).getExecutableArgumentInfo();
	}

	/**
	 * Evaluates a given java expression in the context provided by {@code thisValue}.
	 *
	 * @throws ParseException
	 */
	public static  Object evaluate(String javaExpression, ParserSettings settings, Object thisValue) throws ParseException {
		ParseResult parseResult;

		if (!settings.isEnableDynamicTyping()) {
			// First iteration without evaluation to avoid side effects when errors occur
			parseResult = parse(javaExpression, settings, ParseMode.WITHOUT_EVALUATION,-1, thisValue);
			if (parseResult.getResultType() == ParseResultType.OBJECT_PARSE_RESULT) {
				// Second iteration with evaluation (side effects cannot be avoided)
				parseResult = parse(javaExpression, settings, ParseMode.EVALUATION,-1, thisValue);
			}
		} else {
			parseResult = parse(javaExpression, settings, ParseMode.EVALUATION,-1, thisValue);
		}

		switch (parseResult.getResultType()) {
			case OBJECT_PARSE_RESULT: {
				ObjectParseResult result = (ObjectParseResult) parseResult;
				if (result.getPosition() != javaExpression.length()) {
					throw new ParseException(result.getPosition(), "Unexpected character");
				} else {
					return result.getObjectInfo().getObject();
				}
			}
			case CLASS_PARSE_RESULT: {
				throw new IllegalStateException("Internal error: Class parse results should have been transformed to ParseErrors in AbstractEntityParser.parse()");
			}
			case PARSE_ERROR: {
				ParseError error = (ParseError) parseResult;
				throw new ParseException(error.getPosition(), error.getMessage(), error.getThrowable());
			}
			case AMBIGUOUS_PARSE_RESULT: {
				AmbiguousParseResult result = (AmbiguousParseResult) parseResult;
				throw new ParseException(result.getPosition(), result.getMessage());
			}
			case COMPLETION_SUGGESTIONS: {
				throw new IllegalStateException("Internal error: Unexpected code completion");
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + parseResult.getResultType());
		}
	}

	private static CompletionSuggestions getCompletionSuggestions(String javaExpression, int caretPosition, ParserSettings settings, Object thisValue) throws ParseException {
		ParseResult parseResult = parse(javaExpression, settings, ParseMode.CODE_COMPLETION, caretPosition, thisValue);

		switch (parseResult.getResultType()) {
			case OBJECT_PARSE_RESULT: {
				if (parseResult.getPosition() != javaExpression.length()) {
					throw new ParseException(parseResult.getPosition(), "Unexpected character");
				} else {
					throw new IllegalStateException("Internal error: No completions available");
				}
			}
			case CLASS_PARSE_RESULT: {
				throw new IllegalStateException("Internal error: Class parse results should have been transformed to ParseErrors in AbstractEntityParser.parse()");
			}
			case PARSE_ERROR: {
				ParseError error = (ParseError) parseResult;
				throw new ParseException(error.getPosition(), error.getMessage());
			}
			case AMBIGUOUS_PARSE_RESULT: {
				AmbiguousParseResult result = (AmbiguousParseResult) parseResult;
				throw new ParseException(result.getPosition(), result.getMessage());
			}
			case COMPLETION_SUGGESTIONS: {
				return (CompletionSuggestions) parseResult;
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + parseResult.getResultType());
		}
	}

	private static ParseResult parse(String javaExpression, ParserSettings settings, ParseMode parseMode, int caretPosition, Object thisValue) {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(thisValue, InfoProvider.UNKNOWN_TYPE);
		ParserToolbox parserPool  = new ParserToolbox(thisInfo, settings, parseMode);
		TokenStream tokenStream = new TokenStream(javaExpression, caretPosition);
		try {
			return parserPool.getExpressionParser().parse(tokenStream, thisInfo, ParseExpectation.OBJECT);
		} catch (Exception e) {
			String exceptionClassName = e.getClass().getSimpleName();
			String exceptionMessage = e.getMessage();

			StringBuilder logMessageBuilder = new StringBuilder();
			logMessageBuilder.append(exceptionClassName);
			if (exceptionMessage != null) {
				logMessageBuilder.append(": ").append(exceptionMessage);
			}
			for (StackTraceElement element : e.getStackTrace()) {
				logMessageBuilder.append("\n").append(element);
			}
			settings.getLogger().log(ParserLoggers.createLogEntry(LogLevel.ERROR, JavaParser.class.getSimpleName(), logMessageBuilder.toString()));

			String message = exceptionClassName;
			if (exceptionMessage != null) {
				message += ("\n" + exceptionMessage);
			}
			return ParseResults.createParseError(-1, message, ErrorPriority.EVALUATION_EXCEPTION, e);
		}
	}
}
