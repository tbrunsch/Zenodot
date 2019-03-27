package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodOverloadTest extends CompletionTest
{
	public MethodOverloadTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		final String hashCode = "hashCode()";
		final String toString = "toString()";

		Object testInstance = new TestClassC();
		return new CompletionTestBuilder()
			.testInstance(testInstance)
			.addTest("getTestClass(",							"intValue", "stringValue")
			.addTest("getTestClass(i",							"intValue", "int")
			.addTest("getTestClass(s",							"stringValue", "short")
			.addTest("getTestClass(intValue,",					"stringValue", toString, "intValue")
			.addTest("getTestClass(stringValue,",				"intValue", hashCode, "stringValue")
			.addTest("getTestClass(intValue,stringValue).",		"myInt")
			.addTest("getTestClass(stringValue,intValue).",		"myString")
			.build();
	}

	private static class TestClassA
	{
		private int myInt;
	}

	private static class TestClassB
	{
		private String myString;
	}

	private static class TestClassC
	{
		private int intValue;
		private String stringValue;

		TestClassA getTestClass(int i, String s) { return null; }
		TestClassB getTestClass(String s, int i) { return null; }
	}
}
