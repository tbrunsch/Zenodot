package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.common.AbstractTest;
import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class NoExceptionTest extends CompletionTest
{
	public NoExceptionTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		/*
		 * Just test that there are no exceptions
		 */
		Object testInstance = new TestClass();
		return new CompletionTestBuilder().testInstance(testInstance)
			.configurator(AbstractTest::printLogEntriesAtError)
			.addTest(" ")
			.addTest("")
			.addTest("test()")
			.build();
	}

	private static class TestClass
	{
		private double x = 2.72;

		private String test() { return ""; }
	}
}
