package dd.kms.zenodot.common;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserConsoleLogger;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.settings.ParserSettingsUtils;
import dd.kms.zenodot.api.settings.Variable;
import dd.kms.zenodot.impl.debug.ParserLoggers;
import org.junit.Assert;

import java.util.Arrays;

public class AbstractTest<T extends AbstractTest>
{
	public static final boolean	SKIP_UNSTABLE_TESTS	= "true".equalsIgnoreCase(System.getProperty("skipUnstableTests"));

	protected final Object					testInstance;
	protected final ParserSettingsBuilder	settingsBuilder			= ParserSettingsUtils.createBuilder().minimumAccessLevel(AccessModifier.PRIVATE);

	private boolean							stopAtError				= false;
	private boolean							printLogEntriesAtError	= false;

	protected AbstractTest(Object testInstance) {
		this.testInstance = testInstance;
	}

	public void variables(Variable... variables) {
		settingsBuilder.variables(ImmutableList.copyOf(variables));
	}

	public void minimumAccessLevel(AccessModifier minimumAccessLevel) {
		settingsBuilder.minimumAccessLevel(minimumAccessLevel);
	}

	public void importClasses(String... classNames) {
		try {
			settingsBuilder.importClassesByName(Arrays.asList(classNames));
		} catch (ClassNotFoundException e) {
			Assert.fail("ClassNotFoundException: " + e.getMessage());
		}
	}

	public void importPackages(String... packageNames) {
		settingsBuilder.importPackagesByName(Arrays.asList(packageNames));
	}

	public void customHierarchyRoot(ObjectTreeNode root) {
		settingsBuilder.customHierarchyRoot(root);
	}

	public void enableDynamicTyping() {
		settingsBuilder.enableDynamicTyping(true);
	}

	public void enableConsideringAllClassesForClassCompletions() {
		settingsBuilder.considerAllClassesForClassCompletions(true);
	}

	public void stopAtError() {
		stopAtError = true;
	}

	public void printLogEntriesAtError() {
		printLogEntriesAtError = true;
	}

	protected ParserLogger prepareLogger(boolean printToConsole, int numLoggedEntriesToStopAfter) {
		ParserLogger logger = printToConsole
									? new ParserConsoleLogger().printNumberOfLoggedEntries(true)
									: ParserLoggers.createNullLogger();
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
