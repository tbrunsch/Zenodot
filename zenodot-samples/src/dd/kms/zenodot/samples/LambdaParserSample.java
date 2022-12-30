package dd.kms.zenodot.samples;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * This sample demonstrates how to evaluate lambdas for a specific functional interface.
 */
public class LambdaParserSample
{
	public static void main(String[] args) throws ParseException {
		ParserSettings settings = ParserSettingsBuilder.create().build();

		// create a lambda parser for Comparator<String> where compare() takes two String parameters
		ExpressionParser parser = Parsers.createExpressionParserBuilder(settings)
			.createLambdaParser(Comparator.class, String.class, String.class);

		// create a comparator that compares strings by considering them as numbers
		String expression = "(s1, s2) -> Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2))";
		Comparator<String> comparator = (Comparator<String>) parser.evaluate(expression, null);

		// sort strings by considering them as numbers
		List<String> numbersAsStrings = Arrays.asList("123", "42", "0", "99");
		numbersAsStrings.sort(comparator);
		System.out.println(numbersAsStrings);
	}
}
