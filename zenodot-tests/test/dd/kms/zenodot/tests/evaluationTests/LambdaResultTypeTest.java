package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.api.CompiledLambdaExpression;
import dd.kms.zenodot.api.LambdaExpressionParser;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LambdaResultTypeTest
{
	@Test
	public void testSupplier() throws Exception {
		Random random = new Random();
		Class<?> lambdaResultType = getLambdaResultType(Supplier.class, "() -> this.nextDouble()", random);
		Assert.assertEquals(double.class, lambdaResultType);
	}

	@Test
	public void testConsumer() throws Exception {
		List<String> tokens = new ArrayList<>();
		Class<?> lambdaResultType = getLambdaResultType(Consumer.class, "s -> this.add(s)", tokens);
		Assert.assertEquals(void.class, lambdaResultType);
	}

	@Test
	public void testFunction() throws Exception {
		Class<?> lambdaResultType = getLambdaResultType(Function.class, "t -> ((String) t).length()");
		Assert.assertEquals(int.class, lambdaResultType);
	}

	@Test
	public void testTypedFunction() throws Exception {
		// no need to cast t to String as in testFunction() because we tell the parser the correct parameter type
		Class<?> lambdaResultType = getLambdaResultType(Function.class, "t -> t.length()", null, String.class);
		Assert.assertEquals(int.class, lambdaResultType);
	}

	@Test
	public void testComparator() throws Exception {
		Class<?> lambdaResultType = getLambdaResultType(Comparator.class, "(x, y) -> Integer.compare((int) x, (int) y)");
		Assert.assertEquals(int.class, lambdaResultType);
	}

	@Test
	public void testTypedComparator() throws Exception {
		// no need to cast x and y to int as in testComparator() because we tell the parser the correct parameter types
		Class<?> lambdaResultType = getLambdaResultType(Comparator.class, "(x, y) -> Integer.compare(x, y)", null, int.class, int.class);
		Assert.assertEquals(int.class, lambdaResultType);
	}

	private static Class<?> getLambdaResultType(Class<?> functionalInterface, String lambdaExpression) throws Exception {
		return getLambdaResultType(functionalInterface, lambdaExpression, null);
	}

	private static Class<?> getLambdaResultType(Class<?> functionalInterface, String lambdaExpression, Object thisValue, Class<?>... parameterTypes) throws Exception {
		ParserSettings parserSettings = ParserSettingsBuilder.create().build();
		LambdaExpressionParser<?> lambdaParser = Parsers.createExpressionParserBuilder(parserSettings).createLambdaParser(functionalInterface, parameterTypes);
		CompiledLambdaExpression<?> compiledExpression = lambdaParser.compile(lambdaExpression, thisValue);
		return compiledExpression.getLambdaResultType();
	}
}
