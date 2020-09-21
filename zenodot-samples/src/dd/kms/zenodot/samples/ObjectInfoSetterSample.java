package dd.kms.zenodot.samples;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

/**
 * This sample demonstrates one usage of the wrapper class {@link ObjectInfo}: It does
 * not only describe an object, but also a setter. Since the context that has to be specified
 * when evaluating expression must be an {@link ObjectInfo}, it is possible to transfer
 * information of one evaluation to another evaluation.<br/>
 * <br/>
 * In this sample, we first evaluate an expression that references a field of an object. The
 * evaluated result does not only contain information about the current value, but about the
 * whole field. This information is then used as the context when evaluating a second expression
 * that assigns the context a new value. Since the context is linked to the field, the value
 * of the field is set to the value specified in the expression.
 */
public class ObjectInfoSetterSample
{
	public static void main(String[] args) throws ParseException {
		TestClass testInstance = new TestClass();

		ParserSettings settings = ParserSettingsBuilder.create().build();
		ExpressionParser parser = Parsers.createExpressionParser(settings);

		// First evaluation: Evaluate "this.test" for context testInstance
		String firstExpression = "this.test";
		ObjectInfo thisValue = InfoProvider.createObjectInfo(testInstance);
		ObjectInfo referenceToField = parser.evaluate(firstExpression, thisValue);

		// Second evaluation: Change value of testInstance.test to 7
		String secondExpression = "this = 7";
		parser.evaluate(secondExpression, referenceToField);

		// Value of testInstance.test should now be 7
		System.out.printf("Value of testInstance.test: " + testInstance.test);
	}

	private static class TestClass
	{
		public int test = 5;
	}
}
