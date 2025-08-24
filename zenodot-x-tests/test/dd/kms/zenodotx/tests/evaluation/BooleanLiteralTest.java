package dd.kms.zenodotx.tests.evaluation;

import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTest;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTestBuilder;
import dd.kms.zenodotx.tests.evaluation.framework.TestData;
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
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstanceProvider(TestClass::new);

		testBuilder
			.addTest("true",				true)
			.addTest("false",				false)
			.addTest("getBoolean(true)",	true)
			.addTest("getBoolean(false)",	false);

		testBuilder
			.addTestWithError("getBoolean(tru)", SemanticException.class)
			.addTestWithError("getBoolean(fals)", SemanticException.class)
			.addTestWithError("getBoolean(TRUE)", SemanticException.class)
			.addTestWithError("getBoolean(FALSE)", SemanticException.class);

		return testBuilder.build();
	}

	private static class TestClass
	{
		boolean getBoolean(boolean b) { return b; }
	}
}
