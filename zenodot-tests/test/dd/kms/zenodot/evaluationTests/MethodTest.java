package dd.kms.zenodot.evaluationTests;

import org.junit.Test;

public class MethodTest
{
	@Test
	public void testMethod() {
		Object testInstance = new TestClass();
		new TestExecutor(testInstance)
			.test("getInt()",		3)
			.test("getDouble()",	2.7)
			.test("getString()",	"xyz");
	}

	private static class TestClass
	{
		int getInt() { return 3; }
		double getDouble() { return 2.7; }
		String getString() { return "xyz"; }
	}
}
