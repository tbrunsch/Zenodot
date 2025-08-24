package dd.kms.zenodotx.tests.evaluation;

import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTest;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTestBuilder;
import dd.kms.zenodotx.tests.evaluation.framework.TestData;
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
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstanceProvider(TestClass::new);

		testBuilder
			.addTest("null",			null)
			.addTest("getObject(null)",	null);

		testBuilder
			.addTestWithError("nul", SemanticException.class)
			.addTestWithError("getObject(nul)", SemanticException.class)
			.addTestWithError("getObject(null", SyntaxException.class);

		return testBuilder.build();
	}

	private static class TestClass
	{
		Object getObject(Object o) { return o; }
	}
}
