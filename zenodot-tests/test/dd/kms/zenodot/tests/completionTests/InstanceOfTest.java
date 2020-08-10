package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class InstanceOfTest extends CompletionTest
{
	public InstanceOfTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		String baseString = "\"String\" ";
		String instanceOfString = "instanceof ";
		for (int l = 1; l < instanceOfString.length(); l++) {
			testBuilder
				.addTest(baseString + instanceOfString.substring(0, l), instanceOfString);
		}

		baseString = baseString + instanceOfString + " ";
		String testClassString = "TestClass";
		for (int l = 1; l <= testClassString.length(); l++) {
			testBuilder
				.addTest(baseString + testClassString.substring(0, l), testClassString);
		}

		return testBuilder.build();
	}

	private static class TestClass {}
}
