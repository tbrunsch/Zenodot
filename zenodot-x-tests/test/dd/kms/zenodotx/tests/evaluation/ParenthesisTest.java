package dd.kms.zenodotx.tests.evaluation;

import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTest;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTestBuilder;
import dd.kms.zenodotx.tests.evaluation.framework.TestData;
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
		return new EvaluationTestBuilder()
			.testInstanceProvider(TestClass::new)
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
