package dd.kms.zenodot.tests.evaluationTests;

import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

@RunWith(Parameterized.class)
public class LambdaTest extends EvaluationTest
{
	public LambdaTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		TestClass testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder()
			.testInstance(testInstance)
			.configurator(test -> test.importPackages(
					"java.util",
					"java.util.stream"
				)
			);
		testBuilder
			.addTest("Arrays.asList(1, 2, 3).stream().map(o -> o.toString()).collect(Collectors.toList())",	Arrays.asList("1", "2", "3"))
			.addTest("Arrays.asList(3, 5, 1, 4, 2).stream().sorted((i, j) -> (int) j - (int) i).collect(Collectors.toList())", Arrays.asList(5, 4, 3, 2, 1))
			.addTest("IntStream.range(0, 10).filter(i -> (i % 2) == 0).boxed().collect(Collectors.toSet())", ImmutableSet.of(0, 2, 4, 6, 8))
			.addTest("this.valuesAsStrings(v -> Double.toString(v))", testInstance.valuesAsStrings(Double::toString))
			.addTest("TestClass.stringToInt(TestClass.class.getSimpleName(), s -> ((String) s).length())", TestClass.stringToInt(TestClass.class.getSimpleName(), String::length));

		testBuilder
			// Zenodot cannot infer that i and j are integers
			.addTestWithError("Arrays.asList(3, 5, 1, 4, 2).stream().sorted((i, j) -> j - i).collect(Collectors.toList())")

			// Zenodot cannot infer that s is a String
			.addTestWithError("TestClass.stringToInt(TestClass.class.getSimpleName(), s -> s.length())");

		return testBuilder.build();
	}

	private static class TestClass
	{
		final List<Double>	values = DoubleStream.generate(Math::random).limit(10000).boxed().collect(Collectors.toList());

		Set<String> valuesAsStrings(DoubleFunction<String> toStringConverter) {
			return values.stream().map(toStringConverter::apply).collect(Collectors.toSet());
		}

		static int stringToInt(String s, ToIntFunction<String> stringToIntFunction) {
			return stringToIntFunction.applyAsInt(s);
		}
	}
}