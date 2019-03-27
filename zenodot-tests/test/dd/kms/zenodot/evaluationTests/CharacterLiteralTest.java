package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class CharacterLiteralTest extends EvaluationTest
{
	public CharacterLiteralTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("'x'", 			'x')
			.addTest("getChar('x')",	'x')
			.addTest("getChar('\\'')",	'\'')
			.addTest("getChar('\"')",	'"')
			.addTest("getChar('\\\"')",	'\"')
			.addTest("getChar('\\n')",	'\n')
			.addTest("getChar('\\r')",	'\r')
			.addTest("getChar('\\t')",	'\t');

		testBuilder
			.addTestWithError("getChar(x)")
			.addTestWithError("getChar('x")
			.addTestWithError("getChar('x)")
			.addTestWithError("getChar(x')")
			.addTestWithError("getChar('\')");

		return testBuilder.build();
	}

	private static class TestClass
	{
		char getChar(char c) { return c; }
	}
}
