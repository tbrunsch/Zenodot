package dd.kms.zenodotx.tests.evaluation;

import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTest;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTestBuilder;
import dd.kms.zenodotx.tests.evaluation.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BinaryOperatorShortCircuitEvaluationTest extends EvaluationTest
{
	public BinaryOperatorShortCircuitEvaluationTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstanceProvider(TestClass::new);

		testBuilder
			.addTest("getCounter(FALSE())",					1)
			.addTest("getCounter(FALSE() && FALSE())",		1)
			.addTest("getCounter(FALSE() && TRUE())",			1)
			.addTest("getCounter(TRUE() && FALSE())",			2)
			.addTest("getCounter(TRUE() && TRUE())",			2)
			.addTest("getCounter(FALSE() || FALSE())",		2)
			.addTest("getCounter(FALSE() || TRUE())",			2)
			.addTest("getCounter(TRUE() || FALSE())",			1)
			.addTest("getCounter(TRUE() || TRUE())",			1)
			.addTest("npeTrigger != null && npeTrigger.counter > 0",	false);

		testBuilder
			.addTestWithError("getCounter(FALSE() && 5", SemanticException.class)
			.addTestWithError("getCounter(TRUE() || 'X'", SemanticException.class)
			.addTestWithError("getCounter(FALSE() && false", SyntaxException.class);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private int counter 				= 0;
		private final TestClass npeTrigger	= null;

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
