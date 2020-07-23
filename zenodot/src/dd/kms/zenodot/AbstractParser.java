package dd.kms.zenodot;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLoggers;
import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

abstract class AbstractParser<T extends ParseResult, S extends ParseResultExpectation<T>>
{
	final String			text;
	final ParserSettings	settings;

	AbstractParser(String text, ParserSettings settings) {
		this.text = text;
		this.settings = settings;
	}

	abstract T doParse(TokenStream tokenStream, ParserToolbox parserToolbox, S parseResultExpectation) throws AmbiguousParseResultException, CodeCompletionException, InternalErrorException, InternalEvaluationException, InternalParseException;

	CodeCompletions getCodeCompletions(ObjectInfo thisValue, int caretPosition, S parseResultExpectation) throws ParseException {
		if (caretPosition < 0 || caretPosition > text.length()) {
			throw new IllegalStateException("Invalid caret position");
		}
		TokenStream tokenStream = new TokenStream(text, caretPosition);
		try {
			parse(tokenStream, thisValue, parseResultExpectation);
			String parsedString = tokenStream.toString();
			tokenStream.readRemainingWhitespaces(TokenStream.NO_COMPLETIONS, "Unexpected characters after " + parsedString);
			throw new InternalErrorException("Missed caret position for suggesting code completions");
		} catch (CodeCompletionException e) {
			return e.getCompletions();
		} catch (AmbiguousParseResultException | InternalErrorException | InternalEvaluationException | InternalParseException e) {
			throw new ParseException(tokenStream.getPosition(), e.getMessage());
		}
	}

	T parse(TokenStream tokenStream, ObjectInfo thisValue, S parseResultExpectation) throws AmbiguousParseResultException, CodeCompletionException, InternalEvaluationException, InternalParseException, InternalErrorException {
		try {
			ParserToolbox parserToolbox = new ParserToolbox(thisValue, settings);
			return doParse(tokenStream, parserToolbox, parseResultExpectation);
		} catch (AmbiguousParseResultException | CodeCompletionException | InternalErrorException | InternalEvaluationException | InternalParseException e) {
			throw e;
		} catch (Throwable t) {
			String exceptionClassName = t.getClass().getSimpleName();
			String exceptionMessage = t.getMessage();

			StringBuilder logMessageBuilder = new StringBuilder();
			logMessageBuilder.append(exceptionClassName);
			if (exceptionMessage != null) {
				logMessageBuilder.append(": ").append(exceptionMessage);
			}
			for (StackTraceElement element : t.getStackTrace()) {
				logMessageBuilder.append("\n").append(element);
			}
			settings.getLogger().log(ParserLoggers.createLogEntry(LogLevel.ERROR, AbstractParser.class.getSimpleName(), logMessageBuilder.toString()));

			String message = exceptionClassName;
			if (exceptionMessage != null) {
				message += ("\n" + exceptionMessage);
			}
			throw new InternalEvaluationException(message, t);
		}
	}
}
