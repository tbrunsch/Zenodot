package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class FloatingPointLiteralTest extends EvaluationTest
{
	public FloatingPointLiteralTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		return new EvaluationTestBuilder()
			.testInstance(testInstance)
			.addTest("123f",					123f)
			.addTest("getFloat(123f)",			123f)
			.addTest("123e0",					123e0)
			.addTest("getDouble(123e0)",		123e0)
			.addTest("-123e+07",				-123e+07)
			.addTest("getDouble(-123e+07)",		-123e+07)
			.addTest("+123e-13F",				+123e-13F)
			.addTest("getFloat(+123e-13F)",		+123e-13F)
			.addTest("-123.456E1d",				-123.456E1d)
			.addTest("getDouble(-123.456E1d)",	-123.456E1d)
			.addTest("123.d",					123.d)
			.addTest("getDouble(123.d)",		123.d)
			.addTest("123.e2D",					123.e2D)
			.addTest("getDouble(123.e2D)",		123.e2D)
			.addTest("123.456f",				123.456f)
			.addTest("getFloat(123.456f)",		123.456f)
			.addTest("+.1e-1d",					+.1e-1d)
			.addTest("getDouble(+.1e-1d)",		+.1e-1d)
			.addTest("-.2e3f",					-.2e3f)
			.addTest("getFloat(-.2e3f)",		-.2e3f)
			.build();
	}

	private static class TestClass
	{
		float getFloat(float f) { return f; }
		double getDouble(double d) { return d; }
	}
}
