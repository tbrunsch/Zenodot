package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodDotFieldOrMethodTest extends CompletionTest
{
	public MethodDotFieldOrMethodTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClassB();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("getTestClass",										"getTestClass()")
			.addTest("getTestClass(c).",									"d", "i", "getObject()")
			.addTest("getTestClass(c).get",									"getObject()")
			.addTest("getTestClass(c).getObject(",							"c", "s")
			.addTest("getTestClass(c).getObject(getTestClass(c).d).toS",	"toString()")
			.addTest("getTestInterface().der",								"derived()")
			.addTest("getTestInterface().bas",								"base()");

		testBuilder
			.addTestWithError("getTestClazz().",								-1, IllegalStateException.class)
			.addTestWithError("getTestClazz().i",								-1, IllegalStateException.class)
			.addTestWithError("getTestClass(c).getObject(getTestClass(c).d)",	-1, IllegalStateException.class);

		return testBuilder.build();
	}

	private static class TestClassA
	{
		private int 	i	= 1;
		private double	d	= 1.0;

		private Object getObject(double d) { return null; }
	}

	private static class TestClassB
	{
		private short			s			= 1;
		private char			c			= 'A';

		private TestClassA getTestClass(char c) { return null; }
		private TestInterfaceB getTestInterface() { return null; }
	}

	private interface TestInterfaceA
	{
		void base();
	}

	private interface TestInterfaceB extends TestInterfaceA
	{
		void derived();
	}
}
