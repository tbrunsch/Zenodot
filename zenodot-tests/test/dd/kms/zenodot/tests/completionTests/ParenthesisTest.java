package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ParenthesisTest extends CompletionTest
{
	public ParenthesisTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {

		Object testInstance = new TestClass();
		return new CompletionTestBuilder()
			.testInstance(testInstance)
			.addTest("(",							"x", "y", "getFloat()", "goDoNothing()")
			.addTest("(g",							"getFloat()", "goDoNothing()", "getClass()")
			.addTest("(getFloat(y).toString()).le",	"length()")
			.addTest("(dd.kms.zenodot.tests.completionTests.ParenthesisTest.TestC", "TestClass", "Test")
			.addInsertionTest("(dd.kms.zenodot.tests.completionT^.ParenthesisTest.TestC",	'^', "(dd.kms.zenodot.tests.completionTests.ParenthesisTest.TestC")
			.addInsertionTest("(dd.kms.zenodot.tests.completionTests.ParenthesisT^.TestC",	'^', "(dd.kms.zenodot.tests.completionTests.ParenthesisTest.TestC")
			.build();
	}

	private static class TestClass
	{
		private int y = 1;
		private double x = 2.0;

		void goDoNothing() {}
		Float getFloat(int i) { return i + 0.5f; }
	}

	private interface Test {}
}
