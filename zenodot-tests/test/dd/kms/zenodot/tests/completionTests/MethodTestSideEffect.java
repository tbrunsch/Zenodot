package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MethodTestSideEffect
{
	private final EvaluationMode evaluationMode;

	public MethodTestSideEffect(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	@Parameters(name = "{0}")
	public static Object[] getEvaluationModes() {
		return EvaluationMode.values();
	}

	@Test
	public void testMethodSideEffect() {
		TestClass testInstance = new TestClass();
		String expression = "f(g(), s)";
		int caretPosition = expression.length();
		ParserSettings parserSettings = ParserSettingsBuilder.create()
			.minimumAccessModifier(AccessModifier.PACKAGE_PRIVATE)
			.evaluationMode(evaluationMode)
			.build();
		boolean encounteredParseException = false;
		try {
			ObjectInfo thisValue = InfoProvider.createObjectInfo(testInstance);
			Parsers.createExpressionParser(parserSettings).getCompletions(expression, caretPosition, thisValue);
			Assert.fail("Expected ParseException");
		} catch (ParseException ignored) {
			encounteredParseException = true;
		}
		Assert.assertTrue("Did not encounter a parse exception", encounteredParseException);

		if (evaluationMode == EvaluationMode.DYNAMIC_TYPING) {
			Assert.assertEquals("Did not trigger side effect", testInstance.sideEffectCounter, 1);
		} else {
			Assert.assertEquals("Triggered side effect despite parse error", testInstance.sideEffectCounter, 0);
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
