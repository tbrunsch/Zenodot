package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodOverloadTestWithDynamicTyping extends EvaluationTest
{
	public MethodOverloadTestWithDynamicTyping(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTestWithError("getTestClass(getTestClass(i)).myInt")
			.addTestWithError("getTestClass(getTestClass(j)).myString");

		testBuilder
			.configurator(test -> test.evaluationMode(EvaluationMode.DYNAMIC_TYPING))
			.addTest("getTestClass(getTestClass(i)).myInt",		7)
			.addTest("getTestClass(getTestClass(j)).myString",	"abc");

		testBuilder
			.configurator(test -> test.evaluationMode(EvaluationMode.DYNAMIC_TYPING))
			.addTestWithError("getTestClass(getTestClass(i)).myString")
			.addTestWithError("getTestClass(getTestClass(j)).myInt");

		return testBuilder.build();
	}

	private static class MemberClass1
	{
		private final int myInt = 7;
	}

	private static class MemberClass2
	{
		private final String myString = "abc";
	}

	private static class TestClass
	{
		private final int i = 0;
		private final int j = 1;

		Object getTestClass(int i) { return i == 0 ? new MemberClass1() : new MemberClass2(); }

		MemberClass1 getTestClass(MemberClass1 testClass) { return testClass; }
		MemberClass2 getTestClass(MemberClass2 testClass) { return testClass; }
	}
}
