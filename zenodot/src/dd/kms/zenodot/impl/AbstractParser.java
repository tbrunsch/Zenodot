package dd.kms.zenodot.impl;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.framework.result.CodeCompletions;
import dd.kms.zenodot.framework.result.ParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.debug.ParserLoggers;

abstract class AbstractParser<T extends ParseResult, S extends ParseResultExpectation<T>>
{
	final ParserSettings	settings;
	final Variables			variables;

	AbstractParser(ParserSettings settings, Variables variables) {
		this.settings = settings;
		this.variables = variables;
	}

	abstract T doParse(TokenStream tokenStream, ParserToolbox parserToolbox, S parseResultExpectation) throws CodeCompletionException, InternalErrorException, EvaluationException, SyntaxException;

	CodeCompletions getCodeCompletions(String text, int caretPosition, ObjectInfo thisInfo, S parseResultExpectation) throws ParseException {
		if (caretPosition < 0 || caretPosition > text.length()) {
			throw new IllegalStateException("Invalid caret position");
		}
		TokenStream tokenStream = new TokenStream(text, caretPosition);
		try {
			parse(tokenStream, thisInfo, parseResultExpectation);
			String parsedString = tokenStream.toString();
			tokenStream.readRemainingWhitespaces(TokenStream.NO_COMPLETIONS, "Unexpected characters after " + parsedString);
			throw new InternalErrorException("Missed caret position for suggesting code completions");
		} catch (CodeCompletionException e) {
			return e.getCompletions();
		} catch (InternalErrorException | EvaluationException | SyntaxException e) {
			throw new ParseException(tokenStream, e.getMessage(), e);
		}
	}

	T parse(TokenStream tokenStream, ObjectInfo thisInfo, S parseResultExpectation) throws CodeCompletionException, EvaluationException, SyntaxException, InternalErrorException {
		try {
			ParserToolbox parserToolbox = new ParserToolbox(thisInfo, settings, variables);
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
