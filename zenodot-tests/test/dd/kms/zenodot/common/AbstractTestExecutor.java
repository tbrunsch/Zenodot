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

public class AbstractTestExecutor<T extends AbstractTestExecutor>
{
	protected static final boolean	SKIP_UNSTABLE_TESTS	= "true".equalsIgnoreCase(System.getProperty("skipUnstableTests"));

	protected final Object					testInstance;
	protected final ParserSettingsBuilder	settingsBuilder			= ParserSettingsUtils.createBuilder().minimumAccessLevel(AccessModifier.PRIVATE);

	private boolean							stopAtError				= false;
	private boolean							printLogEntriesAtError	= false;

	public AbstractTestExecutor(Object testInstance) {
		this.testInstance = testInstance;
	}

	private T getBuilder() {
		return (T) this;
	}

	public T variables(Variable... variables) {
		settingsBuilder.variables(ImmutableList.copyOf(variables));
		return getBuilder();
	}

	public T importClasses(String... classNames) {
		try {
			settingsBuilder.importClassesByName(Arrays.asList(classNames));
		} catch (ClassNotFoundException e) {
			Assert.fail("ClassNotFoundException: " + e.getMessage());
		}
		return getBuilder();
	}

	public T importPackages(String... packageNames) {
		settingsBuilder.importPackagesByName(Arrays.asList(packageNames));
		return getBuilder();
	}

	public T minimumAccessLevel(AccessModifier minimumAccessLevel) {
		settingsBuilder.minimumAccessLevel(minimumAccessLevel);
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
