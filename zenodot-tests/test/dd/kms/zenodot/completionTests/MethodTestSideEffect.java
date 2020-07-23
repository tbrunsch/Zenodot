package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.Parsers;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.settings.ParserSettingsUtils;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import org.junit.Assert;
import org.junit.Test;

public class MethodTestSideEffect
{
	@Test
	public void testMethodSideEffect() {
		TestClass testInstance = new TestClass();
		String expression = "f(g(), s)";
		int caretPosition = expression.length();
		ParserSettings settings = ParserSettingsUtils.createBuilder().enableDynamicTyping(false).build();
		try {
			ObjectInfo thisValue = InfoProvider.createObjectInfo(testInstance);
			Parsers.createExpressionParser(expression, settings).getCompletions(thisValue, caretPosition);
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
