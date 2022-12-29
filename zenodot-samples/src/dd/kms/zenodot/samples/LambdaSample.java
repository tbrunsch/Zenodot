package dd.kms.zenodot.samples;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

import java.util.Collections;

/**
 * This sample demonstrates how to evaluate expressions that contain lambdas.
 */
public class LambdaSample
{
	public static void main(String[] args) throws ParseException {
		ParserSettings settings = ParserSettingsBuilder.create()
			.importPackages(Collections.singletonList("java.util"))
			.build();
		// s must be cast to String because Zenodot does not infer generic types
		String expression = "Arrays.asList(\"1\", \"2\", \"3\").stream().mapToInt(s -> Integer.parseInt((String) s)).sum()";
		ExpressionParser parser = Parsers.createExpressionParser(settings);
		System.out.println("Result: " + parser.evaluate(expression, null));
	}
}
