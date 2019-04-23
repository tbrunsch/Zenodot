package dd.kms.zenodot.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.debug.ParserConsoleLogger;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.debug.ParserNullLogger;
import dd.kms.zenodot.settings.AccessLevel;
import dd.kms.zenodot.settings.ObjectTreeNode;
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

	public void variables(Variable... variables) {
		settingsBuilder.variables(ImmutableList.copyOf(variables));
	}

	public void minimumAccessLevel(AccessLevel minimumAccessLevel) {
		settingsBuilder.minimumAccessLevel(minimumAccessLevel);
	}

	public void importClasses(String... classNames) {
		settingsBuilder.importClasses(ImmutableSet.copyOf(classNames));
	}

	public void importPackages(String... packageNames) {
		settingsBuilder.importPackages(ImmutableSet.copyOf(packageNames));
	}

	public void customHierarchyRoot(ObjectTreeNode root) {
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
