package dd.kms.zenodot.tests.evaluationTests;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LambdaParserTest
{
	@Test
	public void testSupplier() throws Exception {
		final long seed = 1234567890L;
		Random random1 = new Random(seed);
		Random random2 = new Random(seed);

		Supplier<Object> supplier = parseLambda(Supplier.class, "() -> this.nextDouble()", random1);
		for (int i = 0; i < 100; i++) {
			Assert.assertEquals(random2.nextDouble(), supplier.get());
		}
	}

	@Test
	public void testConsumer() throws Exception {
		List<String> tokens = new ArrayList<>();
		Consumer<Object> consumer = parseLambda(Consumer.class, "s -> this.add(s)", tokens);
		List<String> expectedTokens = ImmutableList.of("This", " ", "is", " ", "a", " ", "test", ".");
		for (String token : expectedTokens) {
			consumer.accept(token);
		}
		Assert.assertEquals(expectedTokens, tokens);
	}

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
		return parseLambda(functionalInterface, lambdaExpression, null);
	}

	private static <T> T parseLambda(Class<T> functionalInterface, String lambdaExpression, Object thisValue) throws Exception {
		ParserSettings parserSettings = ParserSettingsBuilder.create().build();
		ExpressionParser lambdaParser = Parsers.createExpressionParserBuilder(parserSettings).createLambdaParser(functionalInterface);
		CompiledExpression compiledExpression = lambdaParser.compile(lambdaExpression, thisValue);
		Object result = compiledExpression.evaluate(thisValue);
		Assert.assertTrue("parsed result must be of type '" + functionalInterface.getName() + "'", functionalInterface.isInstance(result));
		return functionalInterface.cast(result);
	}
}
