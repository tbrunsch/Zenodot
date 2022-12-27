package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class VariableTest extends EvaluationTest
{
	public VariableTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder()
			.testInstance(testInstance)
			.configurator(test -> {
				test.createVariable("xyz", 15.0, true);
				test.createVariable("abc", "Test", true);
				test.createVariable("modifiableVariable", 42, false);
			});

		testBuilder
			.addTest("b + xyz",											18.0)
			.addTest("xyz * i",											-15000.0)
			.addTest("(int) xyz / f",									6.0f)
			.addTest("b + abc",											"3Test")
			.addTest("abc + i",											"Test-1000")
			.addTest("abc + f",											"Test2.5")
			.addTest("xyz + xyz",										30.0)
			.addTest("abc + abc",										"TestTest")
			.addTest("test(xyz)",										"15.0")
			.addTest("test(abc)",										"Test")
			.addTest("modifiableVariable",								42)
			.addTest("(modifiableVariable = 27) + modifiableVariable",	54);

		testBuilder
			.addTestWithError("xyz = 13")
			.addTestWithError("abc = \"Test\"")
			.addTestWithError("modifiableVariable = \"Test\"");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final byte b = 3;
		private final int i = -1000;
		private final float f = 2.5f;

		String test(Object o) { return o.toString(); }
	}
}
