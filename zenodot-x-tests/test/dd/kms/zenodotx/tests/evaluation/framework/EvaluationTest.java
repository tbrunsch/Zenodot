package dd.kms.zenodotx.tests.evaluation.framework;

import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.CompiledExpression;
import dd.kms.zenodotx.java.ExpressionParser;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.tests.common.AbstractTest;
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

	void testEvaluationWithError(String expression, Class<? extends Exception> expectedExceptionClass, boolean compile) {
		try {
			JavaSettings settings = settingsBuilder.build();
			if (compile) {
				CompiledExpression compiledExpression = ExpressionParser.compile(expression, -1, null, settings);
				// TODO: Consider variables
				compiledExpression.evaluate(settings.getThisInfo().getObject(), null);
			} else {
				ExpressionParser.evaluate(expression, -1, null, settings);
			}
			fail("Expression: " + expression + " - Expected an exception");
		} catch (AssertionError e) {
			throw e;
		} catch (Exception e) {
			assertTrue("Expression: " + expression + " - Expected exception of class '" + expectedExceptionClass.getSimpleName() + "', but caught an exception of class '" + e.getClass().getSimpleName() + "'", expectedExceptionClass.isInstance(e));
		}
	}

	private boolean runTest(String expression, boolean executeAssertions, Object expectedValue, boolean compile) {
		try {
			JavaSettings settings = settingsBuilder.build();
			Object actualValue;
			if (compile) {
				CompiledExpression compiledExpression = ExpressionParser.compile(expression, -1, null, settings);
				// TODO: Consider variables
				actualValue = compiledExpression.evaluate(settings.getThisInfo().getObject(), null);
			} else {
				actualValue = ExpressionParser.evaluate(expression, -1, null, settings);
			}
			if (executeAssertions) {
				assertEquals("Expression: " + expression, expectedValue, actualValue);
			}
			return Objects.equals(expectedValue, actualValue);
		} catch (SyntaxException | SemanticException | EvaluationException | Parser.EventResultException e) {
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
