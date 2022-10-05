package dd.kms.zenodot.samples;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

/**
 * This sample demonstrates how specifying an context influences how expressions are evaluated.
 */
public class ExpressionContextSample
{
	public static void main(String[] args) throws ParseException {
		ParserSettings settings = ParserSettingsBuilder.create().build();
		String expression = "substring(4)";
		ExpressionParser parser = Parsers.createExpressionParser(settings);
		System.out.println("Result: " + parser.evaluate(expression, "Zenodot"));
	}
}
