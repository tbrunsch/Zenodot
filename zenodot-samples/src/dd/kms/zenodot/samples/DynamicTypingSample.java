package dd.kms.zenodot.samples;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

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
			.evaluationMode(EvaluationMode.DYNAMIC_TYPING)
			.build();
		String expression = "getObject().length()";
		ExpressionParser parser = Parsers.createExpressionParser(settings);
		System.out.println("Result: " + parser.evaluate(expression, testInstance));
	}

	private static class TestClass
	{
		public Object getObject() { return "This is a string"; }
	}
}
