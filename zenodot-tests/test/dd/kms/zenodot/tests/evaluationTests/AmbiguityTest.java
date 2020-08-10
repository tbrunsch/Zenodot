package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AmbiguityTest extends EvaluationTest
{
	public AmbiguityTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		TestClass testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTest("get(c)",	testInstance.get(testInstance.c))
			.addTest("get(b)",	testInstance.get(testInstance.b))
			.addTest("get(i)",	testInstance.get(testInstance.i))
			.addTest("get(l)",	testInstance.get(testInstance.l))
			.addTest("get(o1)",	testInstance.get(testInstance.o1))
			.addTest("get(o2)",	testInstance.get(testInstance.o2));

		testBuilder
			.configurator(test -> test.enableDynamicTyping())
			.addTest("get(o1)", testInstance.get(testInstance.o1))
			.addTest("get(o2)", testInstance.get((Float) testInstance.o2));

		testBuilder
			.configurator(null)
			.addTestWithError("get(s)");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final char c = 'A';
		private final byte b = 123;
		private final short s = (short) 1234;
		private final int i = 123456789;
		private final long l = 5000000000L;
		private final Object o1 = new Double(1.23);
		private final Object o2 = new Float(2.34f);

		char get(char c) { return c; }
		byte get(byte b) { return b; }
		int get(int l) { return i; }
		double get(double d) { return d; }
		Object get(Object o) { return o; }
		Object get(Float f) { return f + 1.0f; }
	}
}
