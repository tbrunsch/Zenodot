package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodTest extends CompletionTest
{
	public MethodTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("xy",		formatMethods("xy", "XY", "xy_z", "XYZ", "XYZ", "x", "X"))
			.addTest("XYZ",		formatMethods("XYZ", "XYZ", "XY", "X", "x", "xy"))
			.addTest("X",		formatMethods("X", "x", "XY", "XYZ", "XYZ", "xy_z", "xy"))
			.addTest("XY",		formatMethods("XY", "xy", "XYZ", "XYZ", "xy_z", "X", "x"))
			.addTest("xy_z",	formatMethods("xy_z", "x", "xy", "XY", "X"))
			.addTest("x",		formatMethods("x", "X", "xy_z", "xy", "XY", "XYZ", "XYZ"))
			.addTest("XYW",		formatMethods("XY", "X", "x", "xy"));

		testBuilder
			.addTestWithError("other()",	-1,	IllegalStateException.class)
			.addTestWithError("other(),",		ParseException.class);

		return testBuilder.build();
	}

	private static String formatMethod(String methodName) {
		return methodName + "()";
	}

	private static String[] formatMethods(String... methodNames) {
		return Arrays.stream(methodNames).map(MethodTest::formatMethod).toArray(size -> new String[size]);
	}

	private static abstract class BasicTestClass
	{
		private int 	xy(char c, float f)						{ return 13; }
		private char 	XYZ(float f, int i, double d)			{ return 'W'; }
		private float	X(int i)								{ return 1.0f; }

		private short	other()									{ return 0; }
	}

	private static class TestClass extends BasicTestClass
	{
		private String 	XY(long l, double d)					{ return "27"; }
		private long	xy_z(double d, short s, int i, byte b)	{ return 13; }
		private double	x(double d)								{ return 2.72; }

		private byte	XYZ(char c, double d, boolean b)		{ return 1; }
	}
}
