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
public class MethodDotFieldOrMethodTest extends EvaluationTest
{
	public MethodDotFieldOrMethodTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(new TestClass());

		testBuilder
			.configurator(null)
			.addTest("getTestClass().i",			7)
			.addTest("getTestClass().d",			1.2)
			.addTest("getTestClass().getString()",	"xyz");

		testBuilder
			.configurator(null)
			.addTest("getTestInterface().getBase()",	"base")
			.addTest("getTestInterface().getDerived()",	42);

		testBuilder
			.configurator(null)
			.addTestWithError("getTestClassAsObject().i", SemanticException.class)
			.addTestWithError("getTestClassAsObject().d", SemanticException.class)
			.addTestWithError("getTestClassAsObject().getString()", SemanticException.class);

		testBuilder
			.configurator(test -> test.evaluationMode(EvaluationMode.DYNAMIC_TYPING))
			.addTest("getTestClassAsObject().i",			7)
			.addTest("getTestClassAsObject().d",			1.2)
			.addTest("getTestClassAsObject().getString()",	"xyz");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final int i = 7;
		private final double d = 1.2;

		TestClass getTestClass() { return new TestClass(); }
		Object getTestClassAsObject() { return new TestClass(); }
		String getString() { return "xyz"; }
		TestInterfaceB getTestInterface() { return new TestInterfaceBImpl(); }
	}

	private interface TestInterfaceA
	{
		String getBase();
	}

	private interface TestInterfaceB extends TestInterfaceA
	{
		int getDerived();
	}

	private static class TestInterfaceBImpl implements TestInterfaceB
	{
		@Override
		public String getBase() {
			return "base";
		}

		@Override
		public int getDerived() {
			return 42;
		}
	}
}
