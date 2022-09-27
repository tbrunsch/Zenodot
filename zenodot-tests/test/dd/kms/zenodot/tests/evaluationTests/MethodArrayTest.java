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
public class MethodArrayTest extends EvaluationTest
{
	public MethodArrayTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass(0, 1);
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTest("getTestClasses()[i0].i0", 13)
			.addTest("getTestClasses()[i0].i1", 7)
			.addTest("getTestClasses()[i1].i0", 4)
			.addTest("getTestClasses()[i1].i1", 9);

		testBuilder
			.configurator(null)
			.addTestWithError("getTestClasses()[o].o")
			.addTestWithError("getTestClasses()[o].getI1()")
			.addTestWithError("getTestClasses()[getI1()].o")
			.addTestWithError("getTestClasses()[getI1()].getI1()");

		testBuilder
			.configurator(test -> test.evaluationMode(EvaluationMode.DYNAMIC_TYPING))
			.addTest("getTestClasses()[o].o",				13)
			.addTest("getTestClasses()[o].getI1()",			7)
			.addTest("getTestClasses()[getI1()].o",			4)
			.addTest("getTestClasses()[getI1()].getI1()",	9);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final int i0;
		private final int i1;
		private final Object o;

		TestClass(int i0, int i1) {
			this.i0 = i0;
			this.i1 = i1;
			this.o = i0;
		}

		Object getI1() {
			return i1;
		}

		TestClass[] getTestClasses() {
			return new TestClass[] { new TestClass(13, 7), new TestClass(4, 9) };
		}
	}
}
