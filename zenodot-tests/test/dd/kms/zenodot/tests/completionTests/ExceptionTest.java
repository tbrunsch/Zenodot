package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ExceptionTest extends CompletionTest
{
	public ExceptionTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTest("doSomething(getInt(s), ",	"x");

		testBuilder
			.configurator(null)
			.addTestWithError("String.valueOf(x = 2.0).l", ParseException.class);

		testBuilder
			.configurator(test -> test.minimumFieldAccessModifier(AccessModifier.PACKAGE_PRIVATE))
			.addTestWithError("x = 2.0", ParseException.class);

		testBuilder
			.configurator(test -> test.evaluationMode(EvaluationMode.DYNAMIC_TYPING))
			.addTestWithError("doSomething(getInt(s), ",	ParseException.class)
			.addTestWithError("doSomething(++i)",			ParseException.class)
			.addTestWithError("new TestClass('c').",		ParseException.class);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final String	s = "";
		private final double	x = 1.0;
		private final int		i = 13;

		TestClass() {}

		TestClass(char c) {
			throw new UnsupportedOperationException();
		}

		int getInt(String s) {
			throw new UnsupportedOperationException();
		}

		void doSomething(byte b, String s) {}
		void doSomething(int i, double d) {}
	}
}
