package dd.kms.zenodot.common;

import dd.kms.zenodot.debug.ParserConsoleLogger;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.debug.ParserNullLogger;
import dd.kms.zenodot.settings.AccessLevel;
import dd.kms.zenodot.settings.ObjectTreeNode;
import dd.kms.zenodot.settings.ParserSettingsBuilder;
import dd.kms.zenodot.settings.Variable;

public class AbstractTestExecutor<T extends AbstractTestExecutor>
{
	protected static final boolean	SKIP_UNSTABLE_TESTS	= "true".equalsIgnoreCase(System.getProperty("skipUnstableTests"));

	protected final Object					testInstance;
	protected final ParserSettingsBuilder	settingsBuilder			= new ParserSettingsBuilder().minimumAccessLevel(AccessLevel.PRIVATE);

	private boolean							stopAtError				= false;
	private boolean							printLogEntriesAtError	= false;

	public AbstractTestExecutor(Object testInstance) {
		this.testInstance = testInstance;
	}

	private T getBuilder() {
		return (T) this;
	}

	public T addVariable(Variable variable) {
		settingsBuilder.addVariable(variable);
		return getBuilder();
	}

	public T minimumAccessLevel(AccessLevel minimumAccessLevel) {
		settingsBuilder.minimumAccessLevel(minimumAccessLevel);
		return getBuilder();
	}

	public T importClass(String className) {
		settingsBuilder.importClass(className);
		return getBuilder();
	}

	public T importPackage(String packageName) {
		settingsBuilder.importPackage(packageName);
		return getBuilder();
	}

	public T customHierarchyRoot(ObjectTreeNode root) {
		settingsBuilder.customHierarchyRoot(root);
		return getBuilder();
	}

	public T enableDynamicTyping() {
		settingsBuilder.enableDynamicTyping(true);
		return getBuilder();
	}

	public T stopAtError() {
		stopAtError = true;
		return getBuilder();
	}

	public T printLogEntriesAtError() {
		printLogEntriesAtError = true;
		return getBuilder();
	}

	protected ParserLogger prepareLogger(boolean printToConsole, int numLoggedEntriesToStopAfter) {
		ParserLogger logger = printToConsole
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
