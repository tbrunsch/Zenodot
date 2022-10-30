package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@RunWith(Parameterized.class)
public class LambdaTest extends CompletionTest
{
	public LambdaTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		TestClass testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder()
			.testInstance(testInstance)
			.configurator(test -> test.importPackages(
					"java.util",
					"java.util.stream"
				)
			);
		testBuilder
			.addTest("Arrays.asList(1, 2, 3).stream().map(o -> o.toStr", "toString()")
			.addTest("Arrays.asList(3, 5, 1, 4, 2).stream().sorted((arg1, arg2) -> (int) arg2 - (int) a", "arg1", "arg2")
			.addTest("IntStream.range(0, 10).filter(index -> (ind", "index")
			.addTest("this.valuesAsStrings(v -> Double.toSt", "toString()")
			.addTest("TestClass.stringToInt(TestClass.class.getSimpleName(), s -> ((String) s).len", "length()");

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
