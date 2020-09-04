package dd.kms.zenodot.impl;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.result.ParseResult;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.debug.ParserLoggers;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParserToolbox;

abstract class AbstractParser<T extends ParseResult, S extends ParseResultExpectation<T>>
{
	final ParserSettings	settings;

	AbstractParser(ParserSettings settings) {
		this.settings = settings;
	}

	abstract T doParse(TokenStream tokenStream, ParserToolbox parserToolbox, S parseResultExpectation) throws CodeCompletionException, InternalErrorException, EvaluationException, SyntaxException;

	CodeCompletions getCodeCompletions(String text, int caretPosition, ObjectInfo thisValue, S parseResultExpectation) throws ParseException {
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
		} catch (InternalErrorException | EvaluationException | SyntaxException e) {
			throw new ParseException(tokenStream, e.getMessage());
		}
	}

	T parse(TokenStream tokenStream, ObjectInfo thisValue, S parseResultExpectation) throws CodeCompletionException, EvaluationException, SyntaxException, InternalErrorException {
		try {
			ParserToolbox parserToolbox = new ParserToolbox(thisValue, settings);
			return doParse(tokenStream, parserToolbox, parseResultExpectation);
		} catch (CodeCompletionException | InternalErrorException | EvaluationException | SyntaxException e) {
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
			throw new EvaluationException(message, t);
		}
	}
}
