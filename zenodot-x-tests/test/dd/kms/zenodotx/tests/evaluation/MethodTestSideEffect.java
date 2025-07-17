package dd.kms.zenodotx.tests.evaluation;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.ExpressionParser;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.JavaSettingsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MethodTestSideEffect
{
	private final EvaluationMode	evaluationMode;

	public MethodTestSideEffect(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	@Parameters(name = "{0}")
	public static Object[] getEvaluationModes() {
		return EvaluationMode.values();
	}

	@Test
	public void testSideEffect() {
		TestClass testInstance = new TestClass();
		String expression = "f(g(), s)";

		JavaSettings settings = new JavaSettingsBuilder(InfoProvider.createObjectInfo(testInstance))
			.minimumMethodAccessModifier(AccessModifier.PACKAGE_PRIVATE)
			.evaluationMode(evaluationMode)
			.build();
		boolean encounteredSemanticException = false;
		try {
			ExpressionParser.evaluate(expression, -1, null, settings);
			Assert.fail("Expected ParseException");
		} catch (SemanticException ignored) {
			encounteredSemanticException = true;
		} catch (SyntaxException | EvaluationException | Parser.EventResultException e) {
			Assert.fail("Unexpected exception: " + e);
		}
		Assert.assertTrue("Did not encounter a SemanticException", encounteredSemanticException);

		if (evaluationMode == EvaluationMode.DYNAMIC_TYPING) {
			Assert.assertEquals("Did not trigger side effect", 1, testInstance.sideEffectCounter);
		} else {
			Assert.assertEquals("Triggered side effect despite parse error", 0, testInstance.sideEffectCounter);
		}
	}

	private static class TestClass
	{
		int sideEffectCounter = 0;

		double f(int i, String s) {
			return 1.0;
		}

		int g() {
			return sideEffectCounter++;
		}
	}
}
