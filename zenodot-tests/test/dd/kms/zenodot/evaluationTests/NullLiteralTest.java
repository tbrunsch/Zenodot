package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class NullLiteralTest extends EvaluationTest
{
	public NullLiteralTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("null",			null)
			.addTest("getObject(null)",	null);

		testBuilder
			.addTestWithError("nul")
			.addTestWithError("getObject(nul)")
			.addTestWithError("getObject(null");

		return testBuilder.build();
	}

	private static class TestClass
	{
		Object getObject(Object o) { return o; }
	}
}
