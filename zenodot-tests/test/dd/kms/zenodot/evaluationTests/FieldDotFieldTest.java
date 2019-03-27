package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class FieldDotFieldTest extends EvaluationTest
{
	public FieldDotFieldTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTest("tc.i", 2)
			.addTest("tc.f", 1.3f);

		testBuilder
			.configurator(null)
			.addTestWithError("o.i")
			.addTestWithError("o.f");

		testBuilder
			.configurator(test -> test.enableDynamicTyping())
			.addTest("o.i", 2)
			.addTest("o.f", 1.3f);

		return testBuilder.build();
	}

	private static class MemberClass
	{
		private final int i = 2;
		private final float f = 1.3f;
	}

	private static class TestClass
	{
		private final MemberClass	tc	= new MemberClass();
		private final Object		o	= new MemberClass();
	}
}
