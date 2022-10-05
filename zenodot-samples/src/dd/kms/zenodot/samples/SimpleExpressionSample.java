package dd.kms.zenodot.samples;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

/**
 * This sample demonstrates how to evaluate the expression
 * {@code Math.max(new int[]{ 2+3, 123/3, 1 << 4 }[1], (int) Math.round(2.718E2))}.
 */
public class SimpleExpressionSample
{
	public static void main(String[] args) throws ParseException {
		ParserSettings settings = ParserSettingsBuilder.create().build();
		String expression = "Math.max(new int[]{ 2+3, 123/3, 1 << 4 }[1], (int) Math.round(2.718E2))";
		ExpressionParser parser = Parsers.createExpressionParser(settings);
		System.out.println("Result: " + parser.evaluate(expression, null));
	}
}
