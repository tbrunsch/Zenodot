package dd.kms.zenodot.evaluationTests.framework;

import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.Parsers;
import dd.kms.zenodot.common.AbstractTest;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.debug.ParserLoggers;
import dd.kms.zenodot.settings.ParserSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Objects;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public abstract class EvaluationTest extends AbstractTest<EvaluationTest>
{
	private final TestExecutor testExecutor;

	protected EvaluationTest(TestData testData) {
		super(testData.getTestInstance());
		TestConfigurator testConfigurator = testData.getConfigureSettingsFunction();
		if (testConfigurator != null) {
			testConfigurator.configure(this);
		}
		this.testExecutor = testData.getTestExecutor();
	}

	@Test
	public void testEvaluation() {
		testExecutor.executeTest(this);
	}

	void testEvaluation(String expression, Object expectedValue) {
		ParserLogger logger = prepareLogger(false, -1);

		boolean repeatTestAtError = isStopAtError() || isPrintLogEntriesAtError();
		if (!runTest(expression, !repeatTestAtError, expectedValue) && repeatTestAtError) {
			int numLoggedEntries = logger.getNumberOfLoggedEntries();
			prepareLogger(isPrintLogEntriesAtError(), isStopAtError() ? numLoggedEntries : -1);
			runTest(expression, true, expectedValue);
		}
	}

	void testEvaluationWithError(String expression) {
		ParserSettings settings = settingsBuilder.build();

		Class<? extends Exception> expectedExceptionClass = ParseException.class;
		try {
			Parsers.createExpressionParser(expression, settings, testInstance).evaluate();
			fail("Expression: " + expression + " - Expected an exception");
		} catch (ParseException | IllegalStateException e) {
			assertTrue("Expression: " + expression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
		}
	}

	private boolean runTest(String expression, boolean executeAssertions, Object expectedValue) {
		ParserSettings settings = settingsBuilder.build();
		ParserLogger logger = settings.getLogger();

		logger.log(ParserLoggers.createLogEntry(LogLevel.INFO, "Test", "Testing expression '" + expression + "'...\n"));

		try {
			Object actualValue = Parsers.createExpressionParser(expression, settings, testInstance).evaluate();
			if (executeAssertions) {
				assertEquals("Expression: " + expression, expectedValue, actualValue);
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
