package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BooleanLiteralTest extends EvaluationTest
{
	public BooleanLiteralTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("true",				true)
			.addTest("false",				false)
			.addTest("getBoolean(true)",	true)
			.addTest("getBoolean(false)",	false);

		testBuilder
			.addTestWithError("getBoolean(tru)")
			.addTestWithError("getBoolean(fals)")
			.addTestWithError("getBoolean(TRUE)")
			.addTestWithError("getBoolean(FALSE)");

		return testBuilder.build();
	}

	private static class TestClass
	{
		boolean getBoolean(boolean b) { return b; }
	}
}
