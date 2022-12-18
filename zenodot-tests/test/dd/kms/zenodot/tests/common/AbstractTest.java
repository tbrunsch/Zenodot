package dd.kms.zenodot.tests.common;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.debug.ParserConsoleLogger;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.impl.debug.ParserLoggers;
import org.junit.Assert;

import java.util.Arrays;

/**
 * This test uses {@link EvaluationMode#STATIC_TYPING} by default.
 */
public class AbstractTest<T extends AbstractTest>
{
	public static final boolean	SKIP_UNSTABLE_TESTS	= "true".equalsIgnoreCase(System.getProperty("skipUnstableTests"));

	protected final Object					testInstance;
	protected final ParserSettingsBuilder	settingsBuilder			= ParserSettingsBuilder.create()
																			.minimumAccessModifier(AccessModifier.PRIVATE)
																			.evaluationMode(EvaluationMode.STATIC_TYPING);
	protected final Variables				variables				= Variables.create();


	private boolean							stopAtError				= false;
	private boolean							printLogEntriesAtError	= false;

	protected AbstractTest(Object testInstance) {
		this.testInstance = testInstance;
	}

	public void createVariable(String name, Object value) {
		variables.createVariable(name, value);
	}

	public void minimumAccessModifier(AccessModifier minimumAccessModifier) {
		settingsBuilder.minimumAccessModifier(minimumAccessModifier);
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

	public void customHierarchyRoot(ObjectTreeNode root) {
		settingsBuilder.customHierarchyRoot(root);
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
