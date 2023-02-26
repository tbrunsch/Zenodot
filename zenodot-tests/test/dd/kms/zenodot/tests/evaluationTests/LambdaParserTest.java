package dd.kms.zenodot.tests.evaluationTests;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.*;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class LambdaParserTest
{
	@Parameterized.Parameters(name = "Evaluation mode: {0}")
	public static Collection<Object> getEvaluationModes() {
		return Arrays.asList(EvaluationMode.values());
	}

	private final EvaluationMode	evaluationMode;

	public LambdaParserTest(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	@Test
	public void testSupplier() throws Exception {
		final long seed = 1234567890L;
		Random random1 = new Random(seed);
		Random random2 = new Random(seed);

		Supplier<Double> supplier = parseLambda(Supplier.class, "() -> this.nextDouble()", random1);
		for (int i = 0; i < 100; i++) {
			Assert.assertEquals((Double) random2.nextDouble(), supplier.get());
		}

		try {
			parseLambda(Supplier.class, "() -> System.out.println()");
			Assert.fail("Lambda cannot be casted to Supplier");
		} catch (ParseException e) {
			/* expected */
		}
	}

	@Test
	public void testConsumer() throws Exception {
		List<String> tokens = new ArrayList<>();
		Consumer<String> consumer = parseLambda(Consumer.class, "s -> this.add(s)", tokens);
		List<String> expectedTokens = ImmutableList.of("This", " ", "is", " ", "a", " ", "test", ".");
		for (String token : expectedTokens) {
			consumer.accept(token);
		}
		Assert.assertEquals(expectedTokens, tokens);
	}

	@Test
	public void testFunction() throws Exception {
		Function<String, Integer> function = parseLambda(Function.class, "t -> ((String) t).length()");
		for (String s : Arrays.asList("abc", "", "x", "0123456789")) {
			Assert.assertEquals((Integer) s.length(), function.apply(s));
		}
	}

	@Test
	public void testTypedFunction() throws Exception {
		// no need to cast t to String as in testFunction() because we tell the parser the correct parameter type
		Function<String, Integer> function = parseLambda(Function.class, "t -> t.length()", null, String.class);
		for (String s : Arrays.asList("abc", "", "x", "0123456789")) {
			Assert.assertEquals((Integer) s.length(), function.apply(s));
		}
	}

	@Test
	public void testComparator() throws Exception {
		Comparator<Integer> comparator = parseLambda(Comparator.class, "(x, y) -> Integer.compare((int) x, (int) y)");
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				Assert.assertEquals(Integer.compare(i, j), comparator.compare(i, j));
			}
		}
	}

	@Test
	public void testTypedComparator() throws Exception {
		// no need to cast x and y to int as in testComparator() because we tell the parser the correct parameter types
		Comparator<Integer> comparator = parseLambda(Comparator.class, "(x, y) -> Integer.compare(x, y)", null, int.class, int.class);
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				Assert.assertEquals(Integer.compare(i, j), comparator.compare(i, j));
			}
		}
	}

	private <T> T parseLambda(Class<T> functionalInterface, String lambdaExpression) throws Exception {
		return parseLambda(functionalInterface, lambdaExpression, null);
	}

	private <T> T parseLambda(Class<T> functionalInterface, String lambdaExpression, Object thisValue, Class<?>... parameterTypes) throws Exception {
		ParserSettings parserSettings = ParserSettingsBuilder.create().evaluationMode(evaluationMode).build();
		LambdaExpressionParser<T> lambdaParser = Parsers.createExpressionParserBuilder(parserSettings).createLambdaParser(functionalInterface, parameterTypes);
		CompiledLambdaExpression<T> compiledExpression = lambdaParser.compile(lambdaExpression, thisValue);
		T result = compiledExpression.evaluate(thisValue);
		Assert.assertTrue("parsed result must be of type '" + functionalInterface.getName() + "'", functionalInterface.isInstance(result));
		return result;
	}
}
