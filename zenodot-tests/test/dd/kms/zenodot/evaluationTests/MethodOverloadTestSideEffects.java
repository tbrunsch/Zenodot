package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.common.AbstractTest;
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
		/*
		 * Since we trigger side effects, these tests will only work if each of them is only executed once.
		 * Hence, we must either prevent the evaluation or the compilation test from running.
		 * Hack: By enabling dynamic typing, we can avoid the compilation test to run.
		 */
		Object testInstance = new TestClass();
		return new EvaluationTestBuilder()
			.configurator(AbstractTest::enableDynamicTyping)
			.testInstance(testInstance)
			.addTest("f(getInt(), 1.0f)",		1)
			.addTest("f(getInt(), \"Test2\")",	2)
			.addTest("f(getInt(), \"Test3\")",	3)
			.addTest("f(getInt(), 4.0f)",		4)
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
