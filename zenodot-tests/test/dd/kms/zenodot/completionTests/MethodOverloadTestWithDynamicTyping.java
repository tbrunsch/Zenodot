package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodOverloadTestWithDynamicTyping extends CompletionTest
{
	public MethodOverloadTestWithDynamicTyping(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClassC();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTestWithError("getTestClass(getTestClass(i)).", ParseException.class);

		testBuilder
			.configurator(test -> test.enableDynamicTyping())
			.addTest("getTestClass(getTestClass(i)).", "myInt")
			.addTest("getTestClass(getTestClass(j)).", "myString");

		return testBuilder.build();
	}

	private static class TestClassA
	{
		private int myInt;
	}

	private static class TestClassB
	{
		private String myString;
	}

	private static class TestClassC
	{
		private int i = 0;
		private int j = 1;

		Object getTestClass(int i) { return i == 0 ? new TestClassA() : new TestClassB(); }

		TestClassA getTestClass(TestClassA testClass) { return testClass; }
		TestClassB getTestClass(TestClassB testClass) { return testClass; }
	}
}
