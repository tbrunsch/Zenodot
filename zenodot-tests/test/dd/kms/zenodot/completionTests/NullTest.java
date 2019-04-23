package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import dd.kms.zenodot.settings.Variable;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class NullTest extends CompletionTest
{
	public NullTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		Variable nullVariable = new Variable("myNull", null, true);
		CompletionTestBuilder testBuilder = new CompletionTestBuilder()
			.testInstance(testInstance)
			.configurator(test -> test.variables(nullVariable));

		testBuilder
			.addTest("f(",				"myNull", "sNull")
			.addTest("f((String) oN",	"oNull")
			.addTest("sNull.le",		"length()");

		testBuilder
			.addTestWithError("myNull.",	ParseException.class)
			.addTestWithError("null.",		ParseException.class);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private String sNull = null;
		private Object oNull = null;
		private Integer iNull = null;
		private double[] daNull = null;

		int f(String s) { return 0; }
	}
}
