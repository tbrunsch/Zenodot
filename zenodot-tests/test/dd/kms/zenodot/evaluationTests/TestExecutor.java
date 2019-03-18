package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.JavaParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.common.AbstractTestExecutor;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLogEntry;
import dd.kms.zenodot.debug.ParserLoggerIF;
import dd.kms.zenodot.settings.ParserSettings;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Class for creating tests with expected successful code completions
 */
public class TestExecutor extends AbstractTestExecutor<TestExecutor>
{
	public TestExecutor(Object testInstance) {
		super(testInstance);
	}

	public TestExecutor test(String javaExpression, Object expectedValue) {
		ParserLoggerIF logger = prepareLogger(false, -1);

		boolean repeatTestAtError = isStopAtError() || isPrintLogEntriesAtError();
		if (!runTest(javaExpression, !repeatTestAtError, expectedValue) && repeatTestAtError) {
			int numLoggedEntries = logger.getNumberOfLoggedEntries();
			prepareLogger(isPrintLogEntriesAtError(), isStopAtError() ? numLoggedEntries : -1);
			runTest(javaExpression, true, expectedValue);
		}

		return this;
	}

	/**
	 * Used for tests that might fail when not using the bootstrap class loader
	 */
	public TestExecutor unstableTest(String javaExpression, Object expectedValue) {
		if (!SKIP_UNSTABLE_TESTS) {
			test(javaExpression, expectedValue);
		}
		return this;
	}

	private boolean runTest(String javaExpression, boolean executeAssertions, Object expectedValue) {
		ParserSettings settings = settingsBuilder.build();
		ParserLoggerIF logger = settings.getLogger();

		logger.log(new ParserLogEntry(LogLevel.INFO, "Test", "Testing expression '" + javaExpression + "'...\n"));

		JavaParser parser = new JavaParser();
		try {
			Object actualValue = parser.evaluate(javaExpression, settings, testInstance);
			if (executeAssertions) {
				assertEquals("Expression: " + javaExpression, expectedValue, actualValue);
			}
			return Objects.equals(expectedValue, actualValue);
		} catch (ParseException e) {
			if (executeAssertions) {
				fail("Exception during expression evaluation: " + e.getMessage());
			}
			return false;
		}
	}
}
