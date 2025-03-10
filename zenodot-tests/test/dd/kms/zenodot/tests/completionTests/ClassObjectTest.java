package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ClassObjectTest extends CompletionTest
{
	public ClassObjectTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();

		return new CompletionTestBuilder().testInstance(testInstance)
			.addTest("test(TestClass.CL",							"CLOSED")
			.addTest("test(TestClass.cla",							"class")
			.addTest("test(TestClass.class.getSimpleName()).clos",	"closed")
			.addInsertionTest("test(TestCla^.class.getSimpleName()).cl", '^', "test(TestClass.class.getSimpleName()).cl")
			.build();
	}

	private static class TestClass
	{
		private static String 	CLOSED = "closed";

		private String			closed	= "closed";

		TestClass test(String s) { return null;}
	}
}
