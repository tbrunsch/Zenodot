package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class FieldTest extends EvaluationTest
{
	public FieldTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("i", 3)
			.addTest("d", 2.4)
			.addTest("s", "xyz")
			.addTest("l", (long) 1);

		testBuilder
			.addTestWithError("")
			.addTestWithError("xyz")
			.addTestWithError("d,");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final int i = 3;
		private final double d = 2.4;
		private final String s = "xyz";
		private final Object l = 1L;
	}
}
