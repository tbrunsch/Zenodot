package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class StringLiteralTest extends EvaluationTest
{
	public StringLiteralTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("\"xyz\"", "xyz")
			.addTest("getString(\"xyz\")",	"xyz")
			.addTest("getString(\"\\\"\")",	"\"")
			.addTest("getString(\"\\n\")",	"\n")
			.addTest("getString(\"\\r\")",	"\r")
			.addTest("getString(\"\\t\")",	"\t");

		testBuilder
			.addTestWithError("getString(xyz)")
			.addTestWithError("getString(\"xyz")
			.addTestWithError("getString(\"xyz)")
			.addTestWithError("getString(xyz\")")
			.addTestWithError("getString(\"\\\")");

		return testBuilder.build();
	}

	private static class TestClass
	{
		String getString(String s) { return s; }
	}
}
