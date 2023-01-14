package dd.kms.zenodot.samples;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

/**
 * This sample demonstrates how to work with variables
 */
public class VariablesSample
{
	public static void main(String[] args) throws ParseException {
		ParserSettings settings = ParserSettingsBuilder.create().build();
		Variables variables = Variables.create()
			.createVariable("i", 42, true)
			.createVariable("x", 3.14, false)
			.createVariable("test", Object.class, 123, false);
		ExpressionParser parser = Parsers.createExpressionParserBuilder(settings)
			.variables(variables)
			.createExpressionParser();

		System.out.println(parser.evaluate("i", null));		// prints 42

		try {
			parser.evaluate("i = 27", null);
		} catch (ParseException e) {
			System.out.println(e.getMessage());				// exception because i is final
		}

		parser.evaluate("x = 2.72", null);					// sets x to 2.72

		System.out.println(parser.evaluate("x", null));		// prints 2.72

		try {
			parser.evaluate("x = \"Test\"", null);
		} catch (ParseException e) {
			System.out.println(e.getMessage());				// exception because x is assumed to be a double
		}

		parser.evaluate("test = \"Test\"", null);			// sets test to "Test" (no exception because test is declared as Object)

		System.out.println(parser.evaluate("test", null));	// prints "Test"
	}
}
