package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodDotFieldOrMethodTest extends EvaluationTest
{
	public MethodDotFieldOrMethodTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTest("getTestClass().i",			7)
			.addTest("getTestClass().d",			1.2)
			.addTest("getTestClass().getString()",	"xyz");

		testBuilder
			.configurator(null)
			.addTestWithError("getTestClassAsObject().i")
			.addTestWithError("getTestClassAsObject().d")
			.addTestWithError("getTestClassAsObject().getString()");

		testBuilder
			.configurator(test -> test.enableDynamicTyping())
			.addTest("getTestClassAsObject().i",			7)
			.addTest("getTestClassAsObject().d",			1.2)
			.addTest("getTestClassAsObject().getString()",	"xyz");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final int i = 7;
		private final double d = 1.2;

		TestClass getTestClass() { return new TestClass(); }
		Object getTestClassAsObject() { return new TestClass(); }
		String getString() { return "xyz"; }
	}
}
