package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodArgumentsTest extends EvaluationTest
{
	public MethodArgumentsTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTest("doubleAdd(i,d)", 5.5);

		testBuilder
			.configurator(null)
			.addTestWithError("doubleAdd(d,i)");

		testBuilder
			.configurator(null)
			.addTestWithError("objectAdd(i,objectAdd(i,d))");

		testBuilder
			.configurator(test -> test.enableDynamicTyping())
			.addTest("objectAdd(i,objectAdd(i,d))", 8.5);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final int i = 3;
		private final double d = 2.5;

		double doubleAdd(int a, double b) { return a + b; }
		Object objectAdd(int a, double b) { return a + b; }
	}
}
