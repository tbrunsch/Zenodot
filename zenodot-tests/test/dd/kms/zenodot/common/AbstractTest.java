package dd.kms.zenodot.common;

import dd.kms.zenodot.debug.ParserConsoleLogger;
import dd.kms.zenodot.debug.ParserLoggerIF;
import dd.kms.zenodot.debug.ParserNullLogger;
import dd.kms.zenodot.settings.AccessLevel;
import dd.kms.zenodot.settings.ObjectTreeNodeIF;
import dd.kms.zenodot.settings.ParserSettingsBuilder;
import dd.kms.zenodot.settings.Variable;

public class AbstractTest<T extends AbstractTest>
{
	public static final boolean	SKIP_UNSTABLE_TESTS	= "true".equalsIgnoreCase(System.getProperty("skipUnstableTests"));

	protected final Object					testInstance;
	protected final ParserSettingsBuilder	settingsBuilder			= new ParserSettingsBuilder().minimumAccessLevel(AccessLevel.PRIVATE);

	private boolean							stopAtError				= false;
	private boolean							printLogEntriesAtError	= false;

	protected AbstractTest(Object testInstance) {
		this.testInstance = testInstance;
	}

	public void addVariable(Variable variable) {
		settingsBuilder.addVariable(variable);
	}

	public void minimumAccessLevel(AccessLevel minimumAccessLevel) {
		settingsBuilder.minimumAccessLevel(minimumAccessLevel);
	}

	public void importClass(String className) {
		settingsBuilder.importClass(className);
	}

	public void importPackage(String packageName) {
		settingsBuilder.importPackage(packageName);
	}

	public void customHierarchyRoot(ObjectTreeNodeIF root) {
		settingsBuilder.customHierarchyRoot(root);
	}

	public void enableDynamicTyping() {
		settingsBuilder.enableDynamicTyping(true);
	}

	protected void stopAtError() {
		stopAtError = true;
	}

	protected void printLogEntriesAtError() {
		printLogEntriesAtError = true;
	}

	protected ParserLoggerIF prepareLogger(boolean printToConsole, int numLoggedEntriesToStopAfter) {
		ParserLoggerIF logger = printToConsole
									? new ParserConsoleLogger().printNumberOfLoggedEntries(true)
									: new ParserNullLogger();
		logger.stopAfter(numLoggedEntriesToStopAfter);
		settingsBuilder.logger(logger);
		return logger;
	}

	protected boolean isStopAtError() {
		return stopAtError;
	}

	protected boolean isPrintLogEntriesAtError() {
		return printLogEntriesAtError;
	}
}
