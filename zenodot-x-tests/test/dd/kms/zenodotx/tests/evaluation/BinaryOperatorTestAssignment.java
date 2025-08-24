package dd.kms.zenodotx.tests.evaluation;

import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTest;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTestBuilder;
import dd.kms.zenodotx.tests.evaluation.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BinaryOperatorTestAssignment extends EvaluationTest
{
	public BinaryOperatorTestAssignment(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		return new EvaluationTestBuilder()
			.testInstanceProvider(TestClass::new)
			.addTest("d = 7.0",				7.0)
			.addTest("get(d = 7.0).d",		7.0)
			.addTest("f = -1",				-1.f)
			.addTest("get(f = -1).f",			-1.f)
			.addTest("i = 13",				13)
			.addTest("get(i = 13).i",			13)
			.addTest("d = f = i = -3",		-3.0)
			.addTest("get(d = f = i = -3).d",	-3.0)
			.addTest("get(d = f = i = -3).f",	-3.f)
			.addTest("get(d = f = i = -3).i",	-3)
			.build();
	}

	private static class TestClass
	{
		private double 	d = 3.0;
		private float 	f = 2.f;
		private int		i = 5;

		TestClass get(double dummy) {
			return this;
		}
	}
}
