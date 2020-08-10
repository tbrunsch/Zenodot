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
public class MethodArgumentsTest extends CompletionTest
{
	public MethodArgumentsTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("prefix",		"prefixC", "prefixD", "prefixI", "prefixC()", "prefixD()", "prefixI()")
			.addTest("prefixI",		"prefixI", "prefixI()", "prefixC", "prefixD")
			.addTest("prefixD",		"prefixD", "prefixD()", "prefixC", "prefixI")
			.addTest("prefixC",		"prefixC", "prefixC()", "prefixD", "prefixI")
			.addTest("prefixI(",	"prefixD", "prefixD()", "prefixC", "prefixI", "prefixC()", "prefixI()")
			.addTest("prefixD(",	"prefixC", "prefixC()", "prefixD", "prefixI", "prefixD()", "prefixI()")
			.addTest("prefixC(",	"prefixI", "prefixI()", "hashCode()", "prefixC", "prefixC()", "prefixD", "prefixD()")
			.addTest("withoutArgs(");

		testBuilder
			.addTestWithError("prefixI(prefixD)",	-1,	IllegalStateException.class)
			.addTestWithError("prefixD(prefixI)",		ParseException.class)
			.addTestWithError("prefixC(prefixI,",		ParseException.class);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private int 	prefixI	= 1;
		private double 	prefixD	= 1.0;
		private char	prefixC	= 'A';

		private int		prefixI(double arg)	{ return 1; }
		private double	prefixD(char arg)	{ return 1.0; }
		private char	prefixC(int arg)	{ return 'A'; }

		private void	withoutArgs() {}
	}
}
