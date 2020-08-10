package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.api.settings.ParserSettingsUtils;
import dd.kms.zenodot.api.settings.Variable;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class NullTest extends EvaluationTest
{
	public NullTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		Variable myNull = ParserSettingsUtils.createVariable("myNull", null, true);
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder()
			.testInstance(testInstance)
			.configurator(test -> test.variables(myNull));

		testBuilder
			.addTest("f(null)",				0)
			.addTest("f(myNull)",			0)
			.addTest("f(sNull)",			0)
			.addTest("f((String) oNull)",	0)
			.addTest("(String) null",		null);

		testBuilder
			.addTestWithError("f(oNull)")
			.addTestWithError("null + 0")
			.addTestWithError("0 + iNull")
			.addTestWithError("!null")
			.addTestWithError("null.toString()")
			.addTestWithError("sNull.length()")
			.addTestWithError("((TestClass) null).sNull")
			.addTestWithError("daNull[0]");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final String sNull = null;
		private final Object oNull = null;
		private final Integer iNull = null;
		private final double[] daNull = null;

		int f(String s) { return 0; }
	}
}
