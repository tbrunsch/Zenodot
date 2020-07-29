package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsUtils;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import org.junit.Assert;
import org.junit.Test;

public class MethodTestSideEffect
{
	@Test
	public void testSideEffect() {
		TestClass testInstance = new TestClass();
		String expression = "f(g(), s)";
		ParserSettings parserSettings = ParserSettingsUtils.createBuilder().enableDynamicTyping(false).build();
		try {
			ObjectInfo thisValue = InfoProvider.createObjectInfo(testInstance);
			Parsers.createExpressionParser(expression, parserSettings).evaluate(thisValue);
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
