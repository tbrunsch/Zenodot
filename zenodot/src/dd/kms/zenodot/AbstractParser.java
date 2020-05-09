package dd.kms.zenodot;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLoggers;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseMode;

abstract class AbstractParser
{
	final String			text;
	final ParserSettings	settings;

	AbstractParser(String text, ParserSettings settings) {
		this.text = text;
		this.settings = settings;
	}

	abstract ParseOutcome doParse(TokenStream tokenStream, ParseMode parseMode);

	CodeCompletions getCodeCompletions(int caretPosition) throws ParseException {
		ParseOutcome parseOutcome = parse(ParseMode.CODE_COMPLETION, caretPosition);

		switch (parseOutcome.getOutcomeType()) {
			case RESULT: {
				if (parseOutcome.getPosition() != text.length()) {
					throw new ParseException(parseOutcome.getPosition(), "Unexpected character");
				} else if (caretPosition >= 0) {
					return CodeCompletions.none(caretPosition);
				} else {
					throw new IllegalStateException("Internal error: No completions available");
				}
			}
			case ERROR: {
				ParseError error = (ParseError) parseOutcome;
				throw new ParseException(error.getPosition(), error.getMessage());
			}
			case AMBIGUOUS_RESULT: {
				AmbiguousParseResult result = (AmbiguousParseResult) parseOutcome;
				throw new ParseException(result.getPosition(), result.getMessage());
			}
			case CODE_COMPLETIONS: {
				return (CodeCompletions) parseOutcome;
			}
			default:
				throw new IllegalStateException("Unsupported parse outcome type: " + parseOutcome.getOutcomeType());
		}
	}

	ParseOutcome parse(ParseMode parseMode, int caretPosition) {
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
			return ParseOutcomes.createParseError(-1, message, ParseError.ErrorPriority.EVALUATION_EXCEPTION, e);
		}
	}

	void checkParsedWholeText(ParseOutcome parseOutcome) throws ParseException {
		if (parseOutcome.getPosition() != text.length()) {
			throw new ParseException(parseOutcome.getPosition(), "Unexpected character");
		}
	}

	ParseException createInvalidResultTypeException(ParseOutcome parseOutcome) {
		ParseOutcomeType outcomeType = parseOutcome.getOutcomeType();
		switch (outcomeType) {
			case RESULT: {
				ParseResult parseResult = (ParseResult) parseOutcome;
				throw new IllegalStateException("Internal error: Unexpected parse result type: '" + parseResult.getResultType() + "'");
			}
			case ERROR: {
				ParseError error = (ParseError) parseOutcome;
				return new ParseException(error.getPosition(), error.getMessage(), error.getThrowable());
			}
			case AMBIGUOUS_RESULT: {
				AmbiguousParseResult result = (AmbiguousParseResult) parseOutcome;
				return new ParseException(result.getPosition(), result.getMessage());
			}
			case CODE_COMPLETIONS: {
				throw new IllegalStateException("Internal error: Unexpected code completion");
			}
			default:
				throw new IllegalStateException("Unsupported parse outcome type: " + outcomeType);
		}
	}
}
