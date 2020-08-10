package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodDotFieldOrMethodTestWithDynamicTyping extends CompletionTest
{
	public MethodDotFieldOrMethodTestWithDynamicTyping(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		final String getClass = "getClass()";
		testBuilder
			.configurator(null)
			.addTest("getObject().",		"x", "xyz", "getInt()")
			.addTest("getObject().x",		"x", "xyz", "getInt()")
			.addTest("getObject().xy",		"xyz", "x", "getInt()")
			.addTest("getObject().xyz",		"xyz", "x", "getInt()")
			.addTest("getObject().get",		"getInt()", getClass, "x", "xyz");

		testBuilder
			.configurator(test -> test.enableDynamicTyping())
			.addTest("getObject().",		"xy", "x", "xyz", "getDouble()", "getInt()")
			.addTest("getObject().x",		"x", "xy", "xyz", "getDouble()", "getInt()")
			.addTest("getObject().xy",		"xy", "xyz", "x", "getDouble()", "getInt()")
			.addTest("getObject().xyz",		"xyz", "xy", "x", "getDouble()", "getInt()")
			.addTest("getObject().get",		"getDouble()", "getInt()", getClass, "xy", "x", "xyz");

		return testBuilder.build();
	}

	private static class BaseClass
	{
		private int x;
		private int xyz;

		public int getInt() { return 1; }
	}

	private static class DescendantClass extends BaseClass
	{
		private int	xy;

		public double getDouble() { return 1.0; }
	}

	private static class TestClass
	{
		private BaseClass getObject() { return new DescendantClass(); }
	}
}
