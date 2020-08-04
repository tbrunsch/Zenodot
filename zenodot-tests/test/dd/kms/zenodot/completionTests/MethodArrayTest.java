package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodArrayTest extends CompletionTest
{
	public MethodArrayTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		final String hashCode = "hashCode()";
		final String getClass = "getClass()";
		testBuilder
			.addTest("getTestClasses()[",			"xyz", hashCode, "xyzw", "xy")
			.addTest("getTestClasses()[x",			"xyz", "xyzw", "xy", hashCode)
			.addTest("getTestClasses()[xy",			"xy", "xyz", "xyzw", hashCode)
			.addTest("getTestClasses()[xyz",		"xyz", "xyzw", "xy", hashCode)
			.addTest("getTestClasses()[xyzw",		"xyzw", "xyz", "xy", hashCode)
			.addTest("getTestClasses()[get",		"getTestClasses()", getClass, "xyz", hashCode, "xyzw", "xy")
			.addTest("getTestClasses()[xyz].",		"xy", "xyz", "xyzw")
			.addTest("getTestClasses()[xyzw].x",	"xy", "xyz", "xyzw");

		testBuilder
			.addTestWithError("xy[",						ParseException.class)
			.addTestWithError("xyz[",						ParseException.class)
			.addTestWithError("xyzw[",						ParseException.class)
			.addTestWithError("getTestClasses()[xy].",		ParseException.class)
			.addTestWithError("getTestClasses()[xyz]", -1,	IllegalStateException.class)
			.addTestWithError("getTestClasses()[xyz)",		ParseException.class);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private String 		xy		= "xy";
		private int 		xyz		= 7;
		private char		xyzw	= 'W';

		private TestClass[] getTestClasses() { return new TestClass[0]; }
	}
}
