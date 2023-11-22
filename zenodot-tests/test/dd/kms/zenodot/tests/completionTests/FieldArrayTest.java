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
public class FieldArrayTest extends CompletionTest
{
	public FieldArrayTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		final String hashCode = "hashCode()";
		testBuilder
			.addTest("member[",			"xyz", hashCode, "xyzw", "member", "xy")
			.addTest("member[x",		"xyz", "xyzw", "xy", hashCode, "member")
			.addTest("member[xy",		"xy", "xyz", "xyzw", hashCode, "member")
			.addTest("member[xyz",		"xyz", "xyzw", "xy", hashCode, "member")
			.addTest("member[xyzw",		"xyzw", "xyz", "xy", hashCode, "member")
			.addTest("member[mem",		"member", "xyz", hashCode, "xyzw", "xy")
			.addTest("member[xyz].",	"member", "xy", "xyz", "xyzw")
			.addTest("member[xyzw].x",	"xy", "xyz", "xyzw", "member")
			.addInsertionTest("mem@[xyzw].x",	"member[xyzw].x")
			.addTest("member.le",		"length")
			.addInsertionTest("memb@.le",		"member.le");

		testBuilder
			.addTestWithError("xy[",				ParseException.class)
			.addTestWithError("xyz[",				ParseException.class)
			.addTestWithError("xyzw[",				ParseException.class)
			.addTestWithError("member[xy].",		ParseException.class)
			.addTestWithError("member[xyz]",	-1, IllegalStateException.class)
			.addTestWithError("member[xyz)",		ParseException.class);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private String		xy		= "xy";
		private int			xyz		= 7;
		private char		xyzw	= 'W';

		private TestClass[]	member	= null;
	}
}
