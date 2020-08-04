package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class InstanceOfTest extends EvaluationTest
{
	public InstanceOfTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("\"String\" instanceof String", "String" instanceof String)
			.addTest("\"String\" instanceof Object", "String" instanceof Object)
			.addTest("\"String\" instanceof Double", false)
			.addTest("null instanceof Object", null instanceof Object)
			.addTest("s instanceof Object", true)
			.addTest("s instanceof String", true)
			.addTest("l instanceof Object", true)
			.addTest("l instanceof String", false)
			.addTest("o instanceof Object", false)
			.addTest("o instanceof String", false)
			.addTest("test() instanceof String", true)
			.addTest("test() instanceof Double", false);

		testBuilder
			.addTest("(1 == 2) == l instanceof String", true)
			.addTest("(i == 3) == s instanceof String", true)
			.addTest("(Boolean) (s instanceof Object) instanceof Boolean", true)
			.addTest("(Boolean) (s instanceof Object) instanceof String", false);

		testBuilder
			.addTestWithError("1 instanceof Object")
			.addTestWithError("1 instanceof 2")
			.addTestWithError("i instanceof Object")
			.addTestWithError("d instanceof Object")
			.addTestWithError("d + i instanceof Object")
			.addTestWithError("(1 == 2) < l instanceof String")
			.addTestWithError("s instanceof Object instanceof Boolean")
			.addTestWithError("(s instanceof Object) instanceof Boolean");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final int i = 3;
		private final double d = 2.4;
		private final String s = "xyz";
		private final Object l = 1L;
		private final Object o = null;

		private Object test() { return "s"; }
	}
}
