package dd.kms.zenodotx.tests.evaluation;

import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTest;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTestBuilder;
import dd.kms.zenodotx.tests.evaluation.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ThisLiteralTest extends EvaluationTest
{
	public ThisLiteralTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		return new EvaluationTestBuilder()
			.testInstance(new TestClass(23))
			.addTest("this.value",		23)
			.addTest("getValue(this)",	23)
			.build();
	}

	private static class TestClass
	{
		private final int value;

		TestClass(int value) { this.value = value; }
		int getValue(TestClass testInstance) { return testInstance.value; }
	}
}
