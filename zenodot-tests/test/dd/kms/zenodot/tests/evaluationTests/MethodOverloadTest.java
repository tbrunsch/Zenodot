package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodOverloadTest extends EvaluationTest
{
	public MethodOverloadTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("getTestClass(myInt).i",		3)
			.addTest("getTestClass(myString).d",	2.7);

		testBuilder
			.addTestWithError("getTestClass(myInt).d")
			.addTestWithError("getTestClass(myString).i");

		return testBuilder.build();
	}

	private static class MemberClass1
	{
		private final int i = 3;
	}

	private static class MemberClass2
	{
		private final double d = 2.7;
	}

	private static class TestClass
	{
		private final int myInt = 3;
		private final String myString = "xyz";

		MemberClass1 getTestClass(int i) { return new MemberClass1(); }
		MemberClass2 getTestClass(String s) { return new MemberClass2(); }
	}
}
