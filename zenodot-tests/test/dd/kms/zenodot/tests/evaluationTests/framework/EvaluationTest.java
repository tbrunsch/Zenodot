package dd.kms.zenodot.tests.evaluationTests.framework;

import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.impl.debug.ParserLoggers;
import dd.kms.zenodot.tests.common.AbstractTest;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Objects;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public abstract class EvaluationTest extends AbstractTest<EvaluationTest>
{
	private final TestExecutor	testExecutor;

	private boolean				testCompilation	= true;

	protected EvaluationTest(TestData testData) {
		super(testData.getTestInstance());
		TestConfigurator testConfigurator = testData.getConfigureSettingsFunction();
		if (testConfigurator != null) {
			testConfigurator.configure(this);
		}
		this.testExecutor = testData.getTestExecutor();
	}

	protected void skipCompilationTest() {
		this.testCompilation = false;
	}

	@Test
	public void testEvaluation() {
		testExecutor.executeTest(this, false);
	}

	@Test
	public void testCompilation() {
		Assume.assumeTrue("The compilation test has been excluded for this class", testCompilation);
		testExecutor.executeTest(this, true);
	}

	void testEvaluation(String expression, Object expectedValue, boolean compile) {
		ParserLogger logger = prepareLogger(false, -1);

		boolean repeatTestAtError = isStopAtError() || isPrintLogEntriesAtError();
		if (!runTest(expression, !repeatTestAtError, expectedValue, compile) && repeatTestAtError) {
			int numLoggedEntries = logger.getNumberOfLoggedEntries();
			prepareLogger(isPrintLogEntriesAtError(), isStopAtError() ? numLoggedEntries : -1);
			runTest(expression, true, expectedValue, compile);
		}
	}

	void testEvaluationWithError(String expression, boolean compile) {
		ParserSettings settings = settingsBuilder.build();

		Class<? extends Exception> expectedExceptionClass = ParseException.class;
		try {
			ExpressionParser expressionParser = Parsers.createExpressionParserBuilder(settings)
				.variables(variables)
				.createExpressionParser();
			if (compile) {
				CompiledExpression compiledExpression = expressionParser.compile(expression, testInstance);
				compiledExpression.evaluate(testInstance);
			} else {
				expressionParser.evaluate(expression, testInstance);
			}
			fail("Expression: " + expression + " - Expected an exception");
		} catch (ParseException | IllegalStateException e) {
			assertTrue("Expression: " + expression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
		} catch (AssertionError e) {
			throw e;
		} catch (Throwable t) {
			Assume.assumeNoException("Skipped test. Reason: We cannot be sure whether this exception is expected or not", t);
		}
	}

	private boolean runTest(String expression, boolean executeAssertions, Object expectedValue, boolean compile) {
		ParserSettings settings = settingsBuilder.build();
		ParserLogger logger = settings.getLogger();

		logger.log(ParserLoggers.createLogEntry(LogLevel.INFO, "Test", "Testing expression '" + expression + "'...\n"));

		try {
			final Object actualValue;
			ExpressionParser expressionParser = Parsers.createExpressionParserBuilder(settings)
				.variables(variables)
				.createExpressionParser();
			if (compile) {
				CompiledExpression compiledExpression = expressionParser.compile(expression, testInstance);
				actualValue = compiledExpression.evaluate(testInstance);
			} else {
				actualValue = expressionParser.evaluate(expression, testInstance);
			}
			if (executeAssertions) {
				assertEquals("Expression: " + expression, expectedValue, actualValue);
			}
			return Objects.equals(expectedValue, actualValue);
		} catch (ParseException e) {
			if (executeAssertions) {
				e.printStackTrace();
				fail("Exception during expression evaluation: " + e.getMessage());
			}
			return false;
		} catch (Throwable t) {
			if (executeAssertions) {
				t.printStackTrace();
				fail("Unexpected throwable: " + t.getClass().getSimpleName() + ": " + t.getMessage());
			}
			return false;
		}
	}
}
