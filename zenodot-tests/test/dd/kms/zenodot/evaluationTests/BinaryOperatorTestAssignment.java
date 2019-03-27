package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BinaryOperatorTestAssignment extends EvaluationTest
{
	public BinaryOperatorTestAssignment(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("reset().getCounter(FALSE())",						1)
			.addTest("reset().getCounter(FALSE() && FALSE())",			1)
			.addTest("reset().getCounter(FALSE() && TRUE())",			1)
			.addTest("reset().getCounter(TRUE() && FALSE())",			2)
			.addTest("reset().getCounter(TRUE() && TRUE())",			2)
			.addTest("reset().getCounter(FALSE() || FALSE())",			2)
			.addTest("reset().getCounter(FALSE() || TRUE())",			2)
			.addTest("reset().getCounter(TRUE() || FALSE())",			1)
			.addTest("reset().getCounter(TRUE() || TRUE())",			1)
			.addTest("npeTrigger != null && npeTrigger.counter > 0",	false);

		testBuilder
			.addTestWithError("reset().getCounter(FALSE() && 5")
			.addTestWithError("reset().getCounter(TRUE() || 'X'");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private int counter 				= 0;
		private final TestClass npeTrigger	= null;

		TestClass reset() {
			counter = 0;
			return this;
		}

		boolean FALSE() {
			counter++;
			return false;
		}

		boolean TRUE() {
			counter++;
			return true;
		}

		int getCounter(boolean dummy) {
			return counter;
		}
	}
}
