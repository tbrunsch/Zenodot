package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodTest extends EvaluationTest
{
	public MethodTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		return new EvaluationTestBuilder()
			.testInstance(testInstance)
			.addTest("getInt()",	3)
			.addTest("getDouble()",	2.7)
			.addTest("getString()",	"xyz")
			.build();
	}

	private static class TestClass
	{
		int getInt() { return 3; }
		double getDouble() { return 2.7; }
		String getString() { return "xyz"; }
	}
}
