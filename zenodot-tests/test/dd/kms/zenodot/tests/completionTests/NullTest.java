package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.ParseException;
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
		CompletionTestBuilder testBuilder = new CompletionTestBuilder()
			.testInstance(testInstance)
			.configurator(test -> test.createVariable("nullV", null, false));

		testBuilder
			.addTest("f(nu",				"nullS", "nullV")
			.addTest("nullS.le",		"length()");

		testBuilder
			.addTestWithError("nullV.",	ParseException.class)
			.addTestWithError("null.",		ParseException.class);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private String nullS = null;
		private Object nullO = null;
		private Integer nullI = null;
		private double[] nullDA = null;

		int f(String s) { return 0; }
	}
}
