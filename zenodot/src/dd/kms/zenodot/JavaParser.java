package dd.kms.zenodot;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLogEntry;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionField;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionVariable;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static dd.kms.zenodot.result.ParseError.ErrorType;

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
	 * Returns a list of potential code completions for a given java expression at a given caret in the context provided
	 * by {@code valueOfThis}. The list is sorted according to internal matching criteria.
	 *
	 * @throws ParseException
	 */
	public List<CompletionSuggestionIF> suggestCodeCompletion(String javaExpression, int caret, ParserSettings settings, Object valueOfThis) throws ParseException {
		ParseResultIF parseResult = parse(javaExpression, settings, ParseMode.CODE_COMPLETION, caret, valueOfThis);

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
				CompletionSuggestions completionSuggestions = (CompletionSuggestions) parseResult;
				Map<CompletionSuggestionIF, MatchRating> ratedSuggestions = completionSuggestions.getRatedSuggestions();
				List<CompletionSuggestionIF> sortedSuggestions = new ArrayList<>(ratedSuggestions.keySet());
				sortedSuggestions.sort(Comparator.comparingInt(JavaParser::getCompletionSuggestionPriorityByClass));
				sortedSuggestions.sort(Comparator.comparing(ratedSuggestions::get));
				return sortedSuggestions;
			}
			default:
				throw new IllegalStateException("Unsupported parse result type: " + parseResult.getResultType());
		}
	}

	/**
	 * Evaluates a given java expression in the context provided by {@code valueOfThis}.
	 *
	 * @throws ParseException
	 */
	public Object evaluate(String javaExpression, ParserSettings settings, Object valueOfThis) throws ParseException {
		ParseResultIF parseResult;

		if (!settings.isEnableDynamicTyping()) {
			// First iteration without evaluation to avoid side effects when errors occur
			parseResult = parse(javaExpression, settings, ParseMode.WITHOUT_EVALUATION,-1, valueOfThis);
			if (parseResult.getResultType() == ParseResultType.OBJECT_PARSE_RESULT) {
				// Second iteration with evaluation (side effects cannot be avoided)
				parseResult = parse(javaExpression, settings, ParseMode.EVALUATION,-1, valueOfThis);
			}
		} else {
			parseResult = parse(javaExpression, settings, ParseMode.EVALUATION,-1, valueOfThis);
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

	private ParseResultIF parse(String javaExpression, ParserSettings settings, ParseMode parseMode, int caret, Object valueOfThis) {
		ObjectInfo thisInfo = new ObjectInfo(valueOfThis, TypeInfo.UNKNOWN);
		ParserToolbox parserPool  = new ParserToolbox(thisInfo, settings, parseMode);
		TokenStream tokenStream = new TokenStream(javaExpression, caret);
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
			settings.getLogger().log(new ParserLogEntry(LogLevel.ERROR, getClass().getSimpleName(), logMessageBuilder.toString()));

			String message = exceptionClassName;
			if (exceptionMessage != null) {
				message += ("\n" + exceptionMessage);
			}
			return new ParseError(-1, message, ErrorType.EVALUATION_EXCEPTION, e);
		}
	}

	/**
	 * Prefers variables ({@link CompletionSuggestionVariable}) over fields ({@link CompletionSuggestionField}) over other suggestions.
	 */
	private static int getCompletionSuggestionPriorityByClass(CompletionSuggestionIF suggestion) {
		Class<? extends CompletionSuggestionIF> suggestionClass = suggestion.getClass();
		return	suggestionClass == CompletionSuggestionVariable.class	? 0 :
				suggestionClass == CompletionSuggestionField.class		? 1
																		: 2;
	}
}
