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
public class FieldTest extends CompletionTest
{
	public FieldTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("xy",		"xy", "XY", "xy_z", "XYZ", "x", "X")
			.addTest("XYZ",		"XYZ", "XY", "X", "x", "xy")
			.addTest("X",		"X", "x", "XY", "XYZ", "xy_z", "xy")
			.addTest("XY",		"XY", "xy", "XYZ", "xy_z", "X", "x")
			.addTest("xy_z",	"xy_z", "x", "xy", "XY", "X")
			.addTest("x",		"x", "X", "xy_z", "xy", "XY", "XYZ")
			.addTest("XYW",		"XY", "X", "x", "xy");

		testBuilder
			.addTestWithError("xy",		-1,	IllegalStateException.class)
			.addTestWithError("xy,",		ParseException.class);

		return testBuilder.build();
	}

	private static abstract class BasicTestClass
	{
		private int 	xy 		= 13;
		private char 	XYZ		= 'W';
		private float	X		= 1.0f;

		private short	other	= 0;
	}

	private static class TestClass extends BasicTestClass
	{
		private String 	XY 		= "27";
		private long	xy_z	= 13;
		private double	x		= 2.72;
	}
}
