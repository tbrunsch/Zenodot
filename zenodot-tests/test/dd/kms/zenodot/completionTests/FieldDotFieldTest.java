package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class FieldDotFieldTest extends CompletionTest
{
	public FieldDotFieldTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("member.",		"member", "otherMember", "X", "xy", "XYZ")
			.addTest("member.x",	"X", "xy", "XYZ", "member", "otherMember")
			.addTest("member.xy",	"xy", "XYZ", "X", "member", "otherMember")
			.addTest("member.xyz",	"XYZ", "xy", "X", "member", "otherMember")
			.addTest("member.mem",	"member", "otherMember", "X", "xy", "XYZ")
			.addTest("member.oth",	"otherMember", "member", "X", "xy", "XYZ");

		testBuilder
			.addTest("member.otherMember.BA",	"BASE", "DERIVED")
			.addTest("member.otherMember.DER",	"DERIVED", "BASE");

		testBuilder
			.addTestWithError("membeR.",		-1, ParseException.class)
			.addTestWithError("MEMBER.xy",		-1, ParseException.class)
			.addTestWithError("member.xy.XY",	-1, ParseException.class)
			.addTestWithError("member.xy",		-1, IllegalStateException.class);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private int 			xy 			= 13;
		private float			X			= 1.0f;
		private char 			XYZ			= 'W';

		private TestClass 		member		= null;
		private TestInterfaceB	otherMember	= null;
	}


	private interface TestInterfaceA
	{
		int BASE = 42;
	}

	private interface TestInterfaceB extends TestInterfaceA
	{
		String DERIVED = "constant";
	}
}
