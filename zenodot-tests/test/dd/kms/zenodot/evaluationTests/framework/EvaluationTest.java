package dd.kms.zenodot.evaluationTests.framework;

import dd.kms.zenodot.JavaParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.common.AbstractTest;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLogEntry;
import dd.kms.zenodot.debug.ParserLogger;
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

	void testEvaluation(String javaExpression, Object expectedValue) {
		ParserLogger logger = prepareLogger(false, -1);

		boolean repeatTestAtError = isStopAtError() || isPrintLogEntriesAtError();
		if (!runTest(javaExpression, !repeatTestAtError, expectedValue) && repeatTestAtError) {
			int numLoggedEntries = logger.getNumberOfLoggedEntries();
			prepareLogger(isPrintLogEntriesAtError(), isStopAtError() ? numLoggedEntries : -1);
			runTest(javaExpression, true, expectedValue);
		}
	}

	void testEvaluationWithError(String javaExpression) {
		ParserSettings settings = settingsBuilder.build();

		Class<? extends Exception> expectedExceptionClass = ParseException.class;
		JavaParser parser = new JavaParser();
		try {
			parser.evaluate(javaExpression, settings, testInstance);
			fail("Expression: " + javaExpression + " - Expected an exception");
		} catch (ParseException | IllegalStateException e) {
			assertTrue("Expression: " + javaExpression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
		}
	}

	private boolean runTest(String javaExpression, boolean executeAssertions, Object expectedValue) {
		ParserSettings settings = settingsBuilder.build();
		ParserLogger logger = settings.getLogger();

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
