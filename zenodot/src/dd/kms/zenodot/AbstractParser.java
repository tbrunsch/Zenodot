package dd.kms.zenodot;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLoggers;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseMode;

import java.util.Map;

abstract class AbstractParser
{
	final String			text;
	final ParserSettings	settings;

	AbstractParser(String text, ParserSettings settings) {
		this.text = text;
		this.settings = settings;
	}

	abstract ParseResult doParse(TokenStream tokenStream, ParseMode parseMode);

	CompletionSuggestions getCompletionSuggestions(int caretPosition) throws ParseException {
		ParseResult parseResult = parse(ParseMode.CODE_COMPLETION, caretPosition);

		switch (parseResult.getResultType()) {
			case OBJECT_PARSE_RESULT:
			case CLASS_PARSE_RESULT:
			case PACKAGE_PARSE_RESULT: {
				if (parseResult.getPosition() != text.length()) {
					throw new ParseException(parseResult.getPosition(), "Unexpected character");
				} else {
					throw new IllegalStateException("Internal error: No completions available");
				}
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

	ParseResult parse(ParseMode parseMode, int caretPosition) {
		TokenStream tokenStream = new TokenStream(text, caretPosition);
		try {
			return doParse(tokenStream, parseMode);
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
			settings.getLogger().log(ParserLoggers.createLogEntry(LogLevel.ERROR, AbstractParser.class.getSimpleName(), logMessageBuilder.toString()));

			String message = exceptionClassName;
			if (exceptionMessage != null) {
				message += ("\n" + exceptionMessage);
			}
			return ParseResults.createParseError(-1, message, ParseError.ErrorPriority.EVALUATION_EXCEPTION, e);
		}
	}

	Map<CompletionSuggestion, StringMatch> extractNameMatchRatings(Map<CompletionSuggestion, MatchRating> ratedSuggestions) {
		ImmutableMap.Builder<CompletionSuggestion, StringMatch> builder = ImmutableMap.builder();
		for (Map.Entry<CompletionSuggestion, MatchRating> entry : ratedSuggestions.entrySet()) {
			builder.put(entry.getKey(), entry.getValue().getNameMatch());
		}
		return builder.build();
	}

	void checkParsedWholeText(ParseResult parseResult) throws ParseException {
		if (parseResult.getPosition() != text.length()) {
			throw new ParseException(parseResult.getPosition(), "Unexpected character");
		}
	}

	void handleInvalidResultType(ParseResult parseResult) throws ParseException {
		ParseResultType resultType = parseResult.getResultType();
		switch (resultType) {
			case OBJECT_PARSE_RESULT:
			case CLASS_PARSE_RESULT:
			case PACKAGE_PARSE_RESULT: {
				throw new IllegalStateException("Internal error: Unexpected type of parse result: '" + resultType + "'");
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
				throw new IllegalStateException("Unsupported parse result type: " + resultType);
		}
	}
}
