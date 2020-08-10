package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ArrayConstructorTest extends CompletionTest
{
	public ArrayConstructorTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		return new CompletionTestBuilder()
			.testInstance(testInstance)
			.addTest("new int[", 		"i")
			.addTest("new int[]{ ",		"i")
			.addTest("new String[",		"i")
			.addTest("new String[]{ ",	"s")
			.build();
	}

	private static class TestClass
	{
		private final int i = 3;
		private final String s = "X";
	}
}
