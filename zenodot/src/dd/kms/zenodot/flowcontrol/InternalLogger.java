package dd.kms.zenodot.flowcontrol;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.debug.ParserLoggers;

import java.util.HashSet;
import java.util.Set;

public class InternalLogger
{
	private final ParserLogger		logger;

	private final Set<Throwable>	loggedExceptions	= new HashSet<>();

	public InternalLogger(ParserLogger logger) {
		this.logger = logger;
	}

	public void beginChildScope() {
		logger.beginChildScope();
	}

	public void endChildScope() {
		logger.endChildScope();
	}

	public void log(Class<?> contextClass, LogLevel logLevel, String message) {
		logger.log(ParserLoggers.createLogEntry(logLevel, contextClass.getSimpleName(), message));
	}

	public void log(Class<?> contextClass, Throwable t, boolean allowRelogging) {
		if (!allowRelogging && loggedExceptions.contains(t)) {
			return;
		}
		loggedExceptions.add(t);
		StringBuilder builder = new StringBuilder();
		formatException(t, builder);
		logger.log(ParserLoggers.createLogEntry(LogLevel.ERROR, contextClass.getSimpleName(), builder.toString()));
	}

	private void formatException(Throwable t, StringBuilder builder) {
		String exceptionDescription = getExceptionDescription(t);
		builder.append(exceptionDescription).append(": ").append(t.getMessage());
		Throwable cause = t.getCause();
		if (cause != null) {
			builder.append("\n");
			formatException(cause, builder);
		}
	}

	private String getExceptionDescription(Throwable t) {
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
}
