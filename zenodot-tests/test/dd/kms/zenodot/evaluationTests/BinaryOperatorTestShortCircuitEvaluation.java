package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BinaryOperatorTestShortCircuitEvaluation extends EvaluationTest
{
	public BinaryOperatorTestShortCircuitEvaluation(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		return new EvaluationTestBuilder()
			.testInstance(testInstance)
			.addTest("reset().get(d = 7.0).d",			7.0)
			.addTest("reset().get(f = -1).f",			-1.f)
			.addTest("reset().get(i = 13).i",			13)
			.addTest("reset().get(d = f = i = -3).d",	-3.0)
			.addTest("reset().get(d = f = i = -3).f",	-3.f)
			.addTest("reset().get(d = f = i = -3).i",	-3)
			.build();
	}

	private static class TestClass
	{
		private double 	d = 3.0;
		private float 	f = 2.f;
		private int		i = 5;

		TestClass reset() {
			d = 3.0;
			f = 2.f;
			i = 5;
			return this;
		}

		TestClass get(double dummy) {
			return this;
		}
	}
}
