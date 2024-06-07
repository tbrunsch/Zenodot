package dd.kms.zenodot.tests.common;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserConsoleLogger;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.impl.debug.ParserLoggers;
import org.junit.Assert;
import org.junit.Assume;

import java.util.Arrays;

/**
 * This test uses {@link EvaluationMode#STATIC_TYPING} by default.
 */
public class AbstractTest<T extends AbstractTest<?>>
{
	public static final boolean	SKIP_UNSTABLE_TESTS	= "true".equalsIgnoreCase(System.getProperty("skipUnstableTests"));

	protected final Object					testInstance;
	protected final ParserSettingsBuilder	settingsBuilder			= ParserSettingsBuilder.create()
																			.minimumFieldAccessModifier(AccessModifier.PRIVATE)
																			.minimumMethodAccessModifier(AccessModifier.PRIVATE)
																			.evaluationMode(EvaluationMode.STATIC_TYPING);
	protected final Variables				variables				= Variables.create();


	private boolean							stopAtError				= false;
	private boolean							printLogEntriesAtError	= false;

	protected AbstractTest(Object testInstance) {
		this.testInstance = testInstance;
	}

	public ParserSettingsBuilder getSettingsBuilder() {
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
		try {
			settingsBuilder.importClassesByName(Arrays.asList(classNames));
		} catch (ClassNotFoundException e) {
			Assert.fail("ClassNotFoundException: " + e.getMessage());
		}
	}

	public void importPackages(String... packageNames) {
		settingsBuilder.importPackages(Arrays.asList(packageNames));
	}

	public void evaluationMode(EvaluationMode evaluationMode) {
		settingsBuilder.evaluationMode(evaluationMode);
	}

	public void enableConsideringAllClassesForClassCompletions() {
		settingsBuilder.considerAllClassesForClassCompletions(true);
	}

	public void stopAtError() {
		stopAtError = true;
	}

	public void assumeMinimumJavaMajorVersion(int minJavaMajorVersion) {
		int javaMajorVersion = getJavaMajorVersion();
		Assume.assumeTrue("Skipping test because Java version " + javaMajorVersion + " is not at least " + minJavaMajorVersion, javaMajorVersion >= minJavaMajorVersion);
	}

	private int getJavaMajorVersion() {
		String javaVersionString = System.getProperty("java.version");
		String[] versionParts = javaVersionString.split("\\.");
		int majorVersionIndex = "1".equals(versionParts[0])
			? 1		// e.g. "1.8.0_291"
			: 0;	// e.g. "17.0.10"
		return Integer.parseInt(versionParts[majorVersionIndex]);
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
		settingsBuilder.logger(logger);
		return logger;
	}
}
