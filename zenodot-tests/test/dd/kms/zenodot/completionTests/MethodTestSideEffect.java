package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.JavaParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.settings.ParserSettingsUtils;
import org.junit.Assert;
import org.junit.Test;

public class MethodTestSideEffect
{
	@Test
	public void testMethodSideEffect() {
		TestClass testInstance = new TestClass();
		String expression = "f(g(), s)";
		int caretPosition = expression.length();
		ParserSettings parserSettings = ParserSettingsUtils.createBuilder().enableDynamicTyping(false).build();
		try {
			new JavaParser(expression, testInstance, parserSettings).suggestCodeCompletion(caretPosition);
			Assert.fail("Expected ParseException");
		} catch (ParseException ignored) {
			/* expected */
		}
		Assert.assertEquals("Triggered side effect despite parse error", testInstance.sideEffectCounter, 0);
	}

	private static class TestClass
	{
		private int sideEffectCounter = 0;

		double f(int i, String s) {
			return 1.0;
		}

		int g() {
			return sideEffectCounter++;
		}
	}
}
