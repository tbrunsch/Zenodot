package dd.kms.zenodotx.tests.evaluation;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTest;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTestBuilder;
import dd.kms.zenodotx.tests.evaluation.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class UnaryOperatorTest extends EvaluationTest
{
	public UnaryOperatorTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstanceProvider(TestClass::new);

		testBuilder
			.addTest("++b",			(byte) 14)
			.addTest("get(++b).b",	(byte) 14)
			.addTest("++i",			-20)
			.addTest("get(++i).i",	-20)
			.addTest("--b",			(byte) 12)
			.addTest("get(--b).b",	(byte) 12)
			.addTest("--i",			-22)
			.addTest("get(--i).i",	-22)
			.addTest("+b",			13)
			.addTest("+i",			-21)
			.addTest("+f",			2.5f)
			.addTest("-b",			-13)
			.addTest("-i",			21)
			.addTest("-f",			-2.5f)
			.addTest("!false",				true)
			.addTest("!true",				false)
			.addTest("!(false || true)",	false)
			.addTest("!(true && false)",	true)
			.addTest("~12345", 				~12345);

		testBuilder
			.configurator(test -> test.evaluationMode(EvaluationMode.MIXED))
			.addTest("-\"1234567\".length()",		-"1234567".length());

		testBuilder
			.addTestWithError("++f", SemanticException.class)
			.addTestWithError("++j", SemanticException.class)
			.addTestWithError("++s", SemanticException.class)
			.addTestWithError("--f", SemanticException.class)
			.addTestWithError("--j", SemanticException.class)
			.addTestWithError("--s", SemanticException.class)
			.addTestWithError("+s", SemanticException.class)
			.addTestWithError("-s", SemanticException.class)
			.addTestWithError("!1", SemanticException.class)
			.addTestWithError("!null", SemanticException.class)
			.addTestWithError("~f", SemanticException.class);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private byte b = 13;
		private int	i = -21;
		private float f = 2.5f;
		private final String s = "Test";
		private final int j = 123;

		TestClass get(int dummy) { return this; }
	}
}
