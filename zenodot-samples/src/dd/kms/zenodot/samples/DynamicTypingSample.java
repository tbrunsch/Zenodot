package dd.kms.zenodot.samples;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

/**
 * This sample demonstrates how dynamic typing can save casts because
 * Zenodot also considers the runtime type of subexpressions when
 * evaluating expressions.
 */
public class DynamicTypingSample
{
	public static void main(String[] args) throws ParseException {
		TestClass testInstance = new TestClass();

		ParserSettings settings = ParserSettingsBuilder.create()
			.enableDynamicTyping(true)
			.build();
		String expression = "getObject().length()";
		ObjectInfo thisValue = InfoProvider.createObjectInfo(testInstance);
		ExpressionParser parser = Parsers.createExpressionParser(settings);
		System.out.println("Result: " + parser.evaluate(expression, thisValue).getObject());
	}

	private static class TestClass
	{
		public Object getObject() { return "This is a string"; }
	}
}
