package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class FieldArrayTest extends EvaluationTest
{
	public FieldArrayTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTest("array[i0]", 1)
			.addTest("array[i1]", 4)
			.addTest("array[i2]", 3)
			.addTest("array[i3]", 7);

		testBuilder
			.configurator(null)
			.addTestWithError("o[i0]")
			.addTestWithError("o[i1]")
			.addTestWithError("o[i2]")
			.addTestWithError("o[i3]");

		testBuilder
			.configurator(test -> test.evaluationMode(EvaluationMode.MIXED))
			.addTest("o[i0]", 1)
			.addTest("o[i1]", 4)
			.addTest("o[i2]", 3)
			.addTest("o[i3]", 7);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final int i0 = 3;
		private final int i1 = 2;
		private final int i2 = 1;
		private final int i3 = 0;
		private final int[] array = { 7, 3, 4, 1 };
		private final Object o = new int[]{ 7, 3, 4, 1 };
	}
}
