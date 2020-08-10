package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.settings.ParserSettingsUtils;
import dd.kms.zenodot.api.settings.Variable;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
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
		Variable nullVariable = ParserSettingsUtils.createVariable("myNull", null, true);
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
