package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class VarArgsTest extends EvaluationTest
{
	public VarArgsTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("sum(d)",				3.5)
			.addTest("sum(d, i1)",			6.5)
			.addTest("sum(d, i1, i2)",		1.5)
			.addTest("sum(d, i1, i2, i3)",	12.5)
			.addTest("sum(d, i123)",		10.5)
			.addTest("sum(i1, i123)",		10.0);

		testBuilder
			.addTestWithError("sum()")
			.addTestWithError("sum(i1, d)")
			.addTestWithError("sum(d, i123, i1)")
			.addTestWithError("sum(d, i1, i123)")
			.addTestWithError("sum(d, i123, i123)");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private double d = 3.5;
		private final int i1 = 3;
		private final int i2 = -5;
		private final int i3 = 11;
		private final int[] i123 = { 1, 2, 4 };

		double sum(double offset, int... ints) {
			return offset + Arrays.stream(ints).sum();
		}
	}
}
