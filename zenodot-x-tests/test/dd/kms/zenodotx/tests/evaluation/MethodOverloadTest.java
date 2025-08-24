package dd.kms.zenodotx.tests.evaluation;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTest;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTestBuilder;
import dd.kms.zenodotx.tests.evaluation.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class MethodOverloadTest extends EvaluationTest
{
	public MethodOverloadTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		TestClass testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstanceProvider(() -> testInstance);

		testBuilder
			.configurator(null)
			.addTest("get(c)",	testInstance.get(testInstance.c))
			.addTest("get(b)",	testInstance.get(testInstance.b))
			.addTest("get(i)",	testInstance.get(testInstance.i))
			.addTest("get(l)",	testInstance.get(testInstance.l))
			.addTest("get(o1)",	testInstance.get(testInstance.o1))
			.addTest("get(o2)",	testInstance.get(testInstance.o2));

		testBuilder
			.configurator(test -> test.evaluationMode(EvaluationMode.MIXED))
			.addTest("get(o1)", testInstance.get(testInstance.o1))
			.addTest("get(o2)", testInstance.get((Float) testInstance.o2));

		testBuilder
			.configurator(null)
			/*
			 * TODO: Our current understanding of the Java Specification was that different overloads that both can be
			 *       called by widening the argument (here short -> int vs. short -> double) are considered equally good.
			 *       However, it seems that the overload with the narrowest widening is preferred. The types according to
			 *       preference are short, int, long, float, double. In this example, get(int) should be called.
			 *
			 * Potential sources:
			 *   - JLS §15.12.2.5 – Choosing the Most Specific Method (https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.12.2.5)
             *   - JLS §5.1.2 – Widening Primitive Conversion
			 */
			.addTestWithError("get(s)", SemanticException.class);

		testBuilder
			.addTest("getTestClass(myInt).i",		3)
			.addTest("getTestClass(myString).d",	2.7);

		testBuilder
			.addTestWithError("getTestClass(myInt).d", SemanticException.class)
			.addTestWithError("getTestClass(myString).i", SemanticException.class);

		return testBuilder.build();
	}

	private static class MemberClass1
	{
		private final int i = 3;
	}

	private static class MemberClass2
	{
		private final double d = 2.7;
	}

	private static class TestClass
	{
		private final char		c	= 'A';
		private final byte		b	= 123;
		private final short		s	= (short) 1234;
		private final int		i	= 123456789;
		private final long		l	= 5000000000L;
		private final Object	o1	= new Double(1.23);
		private final Object	o2	= new Float(2.34f);

		private final int		myInt		= 3;
		private final String	myString	= "xyz";

		char get(char c) { return c; }
		byte get(byte b) { return b; }
		int get(int l) { return i; }
		double get(double d) { return d; }
		Object get(Object o) { return o; }
		Object get(Float f) { return f + 1.0f; }

		MemberClass1 getTestClass(int i) { return new MemberClass1(); }
		MemberClass2 getTestClass(String s) { return new MemberClass2(); }
	}
}
