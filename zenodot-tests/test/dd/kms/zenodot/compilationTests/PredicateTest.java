package dd.kms.zenodot.compilationTests;

import dd.kms.zenodot.CompiledExpression;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.Parsers;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.settings.ParserSettingsUtils;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@RunWith(Parameterized.class)
public class PredicateTest
{
	private static final ParserSettings	PARSER_SETTINGS	= ParserSettingsUtils.createBuilder().build();

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object> getSizes() {
		return Arrays.asList(10, 100, 1000);
	}

	private final int size;

	public PredicateTest(int size) {
		this.size = size;
	}

	@Test
	public void testEvaluatedPredicate() {
		String expression = "this != null && this > 0.5";
		Predicate<Double> javaCompiledPredicate = d -> d != null && d > 0.5;
		Predicate<Double> evaluatedPredicate = d -> Boolean.TRUE.equals(evaluate(expression, d));

		List<Double> values = generateRandomValues();

		testPredicate(values, evaluatedPredicate, javaCompiledPredicate);
	}

	@Test
	public void testCompiledPredicate() throws Exception {
		String expression = "this != null && this > 0.5";
		CompiledExpression compiledExpression = Parsers.createExpressionCompiler(expression, PARSER_SETTINGS, InfoProvider.createTypeInfo(Double.class)).compile();
		Predicate<Double> javaCompiledPredicate = d -> d != null && d > 0.5;
		Predicate<Double> compiledPredicate = d -> {
			try {
				return (Boolean) compiledExpression.evaluate(InfoProvider.createObjectInfo(d)).getObject();
			} catch (Exception e) {
				return false;
			}
		};

		List<Double> values = generateRandomValues();

		testPredicate(values, compiledPredicate, javaCompiledPredicate);
	}

	private static void testPredicate(List<Double> values, Predicate<Double> predicateToTest, Predicate<Double> predicateToCompareWith) {
		for (Double value : values) {
			boolean testResult = predicateToTest.test(value);
			boolean expectedResult = predicateToCompareWith.test(value);
			Assert.assertEquals("Obtained different results for value " + value, expectedResult, testResult);
		}
	}

	private static Object evaluate(String expression, Object thisValue) {
		try {
			return Parsers.createExpressionParser(expression, PARSER_SETTINGS, InfoProvider.createObjectInfo(thisValue)).evaluate().getObject();
		} catch (ParseException e) {
			Assert.fail("ParseException: " + e.getMessage());
			return null;
		}
	}

	private List<Double> generateRandomValues() {
		List<Double> values = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			double randomValue = Math.random();
			values.add(randomValue);
		}
		return values;
	}
}
