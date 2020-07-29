package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodArgumentsTestWithDynamicTyping extends CompletionTest
{
	public MethodArgumentsTestWithDynamicTyping(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTestWithError("getTestClassObject(getObject()).get", ParseException.class);

		testBuilder
			.configurator(test -> test.enableDynamicTyping())
			.addTest("getTestClassObject(getObject()).get", "getObject()", "getTestClassObject()", "getClass()");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private Object getObject() { return new TestClass(); }
		private Object getTestClassObject(TestClass testClass) { return testClass.getObject(); }
	}
}
