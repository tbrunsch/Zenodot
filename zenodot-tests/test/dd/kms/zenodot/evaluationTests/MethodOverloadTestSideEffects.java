package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodOverloadTestSideEffects extends EvaluationTest
{
	public MethodOverloadTestSideEffects(TestData testData) {
		super(testData);
	}

	/**
	 * It is important that expression are not evaluated multiple times
	 * when searching for the right method overload. Otherwise, side effects
	 * (which are critical anyway) may also occur multiple times.
	 */
	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		return new EvaluationTestBuilder()
			.testInstance(testInstance)
			.addTest("f(getInt(), 1.0f)",		1)
			.addTest("f(getInt(), \"Test\")",	2)
			.addTest("f(getInt(), \"Test\")",	3)
			.addTest("f(getInt(), 1.0f)",		4)
			.build();
	}

	private static class TestClass
	{
		private int count = 0;

		int f(int i, float f) { return i; };
		int f(int i, String s) { return i; };

		int getInt() { return ++count; }
	}
}
