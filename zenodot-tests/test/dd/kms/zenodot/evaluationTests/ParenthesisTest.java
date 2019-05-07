package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ParenthesisTest extends EvaluationTest
{
	public ParenthesisTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		final String getClass = "getClass()";
		return new EvaluationTestBuilder()
			.testInstance(testInstance)
			.addTest("(getFloat(y).toString())",			"1.5")
			.addTest("(getFloat(y)).toString()",			"1.5")
			.addTest("(getFloat(y).toString()).length()",	3)
			.addTest("((x))",								2.0)
			.addTest("(((1.3e-7)))",						1.3e-7)
			.build();
	}

	private static class TestClass
	{
		private final int y = 1;
		private final double x = 2.0;

		void goDoNothing() {}
		Float getFloat(int i) { return i + 0.5f; }
	}
}
