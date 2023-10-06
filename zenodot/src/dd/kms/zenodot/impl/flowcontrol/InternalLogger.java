package dd.kms.zenodot.impl.flowcontrol;

import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.framework.utils.ParseUtils;
import dd.kms.zenodot.impl.debug.ParserLoggers;

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
		String error = ParseUtils.formatException(t, new StringBuilder()).toString();
		logger.log(ParserLoggers.createLogEntry(LogLevel.ERROR, contextClass.getSimpleName(), error));
	}
}
