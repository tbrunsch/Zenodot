package dd.kms.zenodotx.tests.evaluation.framework;

import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.java.rule.JavaRuleSet;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.tests.common.AbstractTest;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public abstract class EvaluationTest extends AbstractTest<EvaluationTest>
{
	private static final JavaRuleSet									RULE_SET		= new JavaRuleSet();
	private static final Rule<Void, InstanceParseResult, JavaSettings>	FULL_EXPRESSION = RULE_SET.getFullExpression();

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
		/*
		TODO
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

		 */
	}

	private boolean runTest(String expression, boolean executeAssertions, Object expectedValue, boolean compile) {
		if (compile) {
			settingsBuilder.evaluationMode(EvaluationMode.STATIC_TYPING);
		} else {
			settingsBuilder.evaluationMode(EvaluationMode.DYNAMIC_TYPING);
		}

		Parser<JavaSettings> parser = new Parser<>(expression, -1, null);

		try {
			InstanceParseResult result = parser.parse(FULL_EXPRESSION, null, settingsBuilder.build());
			ObjectInfo resultInfo;
			if (compile) {
				resultInfo = result.evaluate(settingsBuilder.evaluationMode(EvaluationMode.DYNAMIC_TYPING).build());
			} else {
				resultInfo = result.getEvaluatedResult();
			}
			Object actualValue = resultInfo.getObject();
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
