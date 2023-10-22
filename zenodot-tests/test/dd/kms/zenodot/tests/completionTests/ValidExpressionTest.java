package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ValidExpressionTest extends CompletionTest
{
	public ValidExpressionTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		/*
		 * Test that even valid expressions are not evaluated in completion mode.
		 * (Evaluations will result in an exception, so we only test that no
		 * exception is thrown.)
		 */

		Object testInstance = new TestClass();
		return new CompletionTestBuilder()
			.testInstance(testInstance)
			.configurator(test -> test.createVariable("variable", 13.0, false))
			.addTest("field")
			.addTest("method()")
			.addTest("variable")
			.addTest("TestClass")
			.addTest("new TestClass()")
			.addTest("new int[3]")
			.addTest("new int[]{ 1, 2, 3 }")
			.addTest("null")
			.addTest("5+3")
			.addTest("!false")
			.addTest("(1 << 2)")
			.build();
	}

	private static class TestClass
	{
		private int field;

		void method() {}
	}
}
