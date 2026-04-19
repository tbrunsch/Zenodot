package dd.kms.zenodotx.tests.evaluation;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTest;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTestBuilder;
import dd.kms.zenodotx.tests.evaluation.framework.TestData;
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
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(new TestClass());

		testBuilder
			.configurator(null)
			.addTest("tc.i", 2)
			.addTest("tc.f", 1.3f);

		testBuilder
			.configurator(null)
			.addTest("testInterface.BASE", 		"base")
			.addTest("testInterface.DERIVED",	42);

		testBuilder
			.configurator(null)
			.addTestWithError("o.i",	SemanticException.class)
			.addTestWithError("o.f",	SemanticException.class);

		testBuilder
			.configurator(test -> test.evaluationMode(EvaluationMode.MIXED))
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
		private final MemberClass		tc				= new MemberClass();
		private final Object			o				= new MemberClass();
		private final TestInterfaceB	testInterface	= new TestInterfaceB(){};
	}


	private interface TestInterfaceA
	{
		String BASE = "base";
	}

	private interface TestInterfaceB extends TestInterfaceA
	{
		int DERIVED = 42;
	}
}
