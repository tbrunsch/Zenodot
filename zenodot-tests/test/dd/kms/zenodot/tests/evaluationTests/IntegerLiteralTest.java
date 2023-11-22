package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class IntegerLiteralTest extends EvaluationTest
{
	public IntegerLiteralTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		TestClass testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("120",						120)
			.addTest("-120",					-120)
			.addTest("getByte((byte) 120)",		testInstance.getByte((byte) 120))
			.addTest("1234",					1234)
			.addTest("-1234",					-1234)
			.addTest("getShort((short) 1234)",	testInstance.getShort((short) 1234))
			.addTest("100000",					100000)
			.addTest("-100000",					-100000)
			.addTest("getInt(100000)",			testInstance.getInt(100000))
			.addTest("5000000000L",				5000000000L)
			.addTest("-5000000000L",			-5000000000L)
			.addTest("getLong(5000000000l)",	testInstance.getLong(5000000000l));

		testBuilder
			.addTestWithError("getByte(123)")
			.addTestWithError("getShort(1000)")
			.addTestWithError("getInt(5000000000)");

		return testBuilder.build();
	}

	private static class TestClass
	{
		byte getByte(byte b) { return b; }
		short getShort(short s) { return s; }
		int getInt(int i) { return i; }
		long getLong(long l) { return l; }
	}
}
