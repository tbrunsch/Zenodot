package dd.kms.zenodotx.tests.common;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserConsoleLogger;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.impl.debug.ParserLoggers;
import dd.kms.zenodotx.java.JavaSettingsBuilder;

/**
 * This test uses {@link EvaluationMode#STATIC_TYPING} by default.
 */
public class AbstractTest<T extends AbstractTest<?>>
{
	public static final boolean	SKIP_UNSTABLE_TESTS	= "true".equalsIgnoreCase(System.getProperty("skipUnstableTests"));

	protected final JavaSettingsBuilder		settingsBuilder;
	protected final Variables				variables				= Variables.create();


	private boolean							stopAtError				= false;
	private boolean							printLogEntriesAtError	= false;

	protected AbstractTest(Object testInstance) {
		settingsBuilder = new JavaSettingsBuilder(InfoProvider.createObjectInfo(testInstance));
	}

	public JavaSettingsBuilder getSettingsBuilder() {
		return settingsBuilder;
	}

	public void createVariable(String name, Object value, boolean isFinal) {
		variables.createVariable(name, value, isFinal);
	}

	public void minimumFieldAccessModifier(AccessModifier minimumAccessModifier) {
		settingsBuilder.minimumFieldAccessModifier(minimumAccessModifier);
	}

	public void minimumMethodAccessModifier(AccessModifier minimumAccessModifier) {
		settingsBuilder.minimumMethodAccessModifier(minimumAccessModifier);
	}

	public void importClasses(String... classNames) {
		/*
		TODO
		try {
			settingsBuilder.importClassesByName(Arrays.asList(classNames));
		} catch (ClassNotFoundException e) {
			Assert.fail("ClassNotFoundException: " + e.getMessage());
		}
		 */
	}

	public void importPackages(String... packageNames) {
		/*
		TODO
		settingsBuilder.importPackages(Arrays.asList(packageNames));
		 */
	}

	public void evaluationMode(EvaluationMode evaluationMode) {
		settingsBuilder.evaluationMode(evaluationMode);
	}

	public void enableConsideringAllClassesForClassCompletions() {
		/*
		TODO
		settingsBuilder.considerAllClassesForClassCompletions(true);
		 */
	}

	public void stopAtError() {
		stopAtError = true;
	}

	protected boolean isStopAtError() {
		return stopAtError;
	}

	public void printLogEntriesAtError() {
		printLogEntriesAtError = true;
	}

	protected boolean isPrintLogEntriesAtError() {
		return printLogEntriesAtError;
	}

	protected ParserLogger prepareLogger(boolean printToConsole, int numLoggedEntriesToStopAfter) {
		ParserLogger logger = printToConsole
									? new ParserConsoleLogger().printNumberOfLoggedEntries(true)
									: ParserLoggers.createNullLogger();
		logger.stopAfter(numLoggedEntriesToStopAfter);
		/*
		TODO
		settingsBuilder.logger(logger);
		 */
		return logger;
	}
}
