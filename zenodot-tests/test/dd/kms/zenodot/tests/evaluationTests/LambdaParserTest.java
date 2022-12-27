package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

public class LambdaParserTest
{
	@Test
	public void testFunction() throws Exception {
		Function<Object, Object> function = parseLambda(Function.class, "t -> ((String) t).length()");
		for (String s : Arrays.asList("abc", "", "x", "0123456789")) {
			Object result = function.apply(s);
			Assert.assertEquals(s.length(), result);
		}
	}

	@Test
	public void testComparator() throws Exception {
		Comparator<Object> comparator = parseLambda(Comparator.class, "(x, y) -> Integer.compare((int) x, (int) y)");
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				int comparisonResult = comparator.compare(i, j);
				Assert.assertEquals(Math.signum(i - j), Math.signum(comparisonResult), 0.0);
			}
		}
	}

	private static <T> T parseLambda(Class<T> functionalInterface, String lambdaExpression) throws Exception {
		ParserSettings parserSettings = ParserSettingsBuilder.create().build();
		ExpressionParser lambdaParser = Parsers.createExpressionParserBuilder(parserSettings).createLambdaParser(functionalInterface);
		CompiledExpression compiledExpression = lambdaParser.compile(lambdaExpression, null);
		Object result = compiledExpression.evaluate(null);
		Assert.assertTrue("parsed result must be of type '" + functionalInterface.getName() + "'", functionalInterface.isInstance(result));
		return functionalInterface.cast(result);
	}
}
