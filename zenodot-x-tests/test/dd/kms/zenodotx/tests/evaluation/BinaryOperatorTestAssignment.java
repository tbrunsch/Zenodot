package dd.kms.zenodotx.tests.evaluation;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTest;
import dd.kms.zenodotx.tests.evaluation.framework.EvaluationTestBuilder;
import dd.kms.zenodotx.tests.evaluation.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BinaryOperatorTestAssignment extends EvaluationTest
{
	public BinaryOperatorTestAssignment(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		EvaluationTestBuilder builder = new EvaluationTestBuilder().testInstanceProvider(TestClass::new);

		builder
			.addTest("d = 7.0",				7.0)
			.addTest("get(d = 7.0).d",		7.0)
			.addTest("f = -1",				-1.f)
			.addTest("get(f = -1).f",			-1.f)
			.addTest("i = 13",				13)
			.addTest("get(i = 13).i",			13)
			.addTest("d = f = i = -3",		-3.0)
			.addTest("get(d = f = i = -3).d",	-3.0)
			.addTest("get(d = f = i = -3).f",	-3.f)
			.addTest("get(d = f = i = -3).i",	-3);

		builder
			.configurator(configurator -> configurator.evaluationMode(EvaluationMode.STATIC_TYPING))
			.addTest("a1 = a1", A.A1)
			.addTest("a1 = getA1()", A.A1)
			.addTest("a1 = b1", B.B1)
			.addTest("a1 = getB1()", B.B1)
			.addTest("a1 = b2", B.B2)
			.addTest("a1 = getB2()", B.B2)
			.addTest("b1 = a1", A.A1)
			.addTest("b1 = getA1()", A.A1)
			.addTest("b1 = b1", B.B1)
			.addTest("b1 = getB1()", B.B1)
			.addTest("b1 = b2", B.B2)
			.addTest("b1 = getB2()", B.B2)
			.addTest("b2 = b2", B.B2)
			.addTest("b2 = getB2()", B.B2)
			.addTestWithError("b2 = a1", SemanticException.class)
			.addTestWithError("b2 = getA1()", SemanticException.class)
			.addTestWithError("b2 = b1", SemanticException.class)
			.addTestWithError("b2 = getB1()", SemanticException.class);

		builder
			.configurator(configurator -> configurator.evaluationMode(EvaluationMode.MIXED))
			.addTest("a1 = a1", A.A1)
			.addTest("a1 = getA1()", A.A1)
			.addTest("a1 = b1", B.B1)
			.addTest("a1 = getB1()", B.B1)
			.addTest("a1 = b2", B.B2)
			.addTest("a1 = getB2()", B.B2)
			.addTest("b1 = a1", A.A1)
			.addTest("b1 = getA1()", A.A1)
			.addTest("b1 = b1", B.B1)
			.addTest("b1 = getB1()", B.B1)
			.addTest("b1 = b2", B.B2)
			.addTest("b1 = getB2()", B.B2)
			.addTest("b2 = b1", B.B1)	// works because runtime type of b1 is B
			.addTest("b2 = b2", B.B2)
			.addTest("b2 = getB2()", B.B2)
			.addTestWithError("b2 = a1", SemanticException.class)
			.addTestWithError("b2 = getA1()", SemanticException.class)
			.addTestWithError("b2 = getB1()", SemanticException.class);	// still does not work because runtime type of result of getB1() is not evaluated

		builder
			.configurator(configurator -> configurator.evaluationMode(EvaluationMode.DYNAMIC_TYPING))
			.addTest("a1 = a1", A.A1)
			.addTest("a1 = getA1()", A.A1)
			.addTest("a1 = b1", B.B1)
			.addTest("a1 = getB1()", B.B1)
			.addTest("a1 = b2", B.B2)
			.addTest("a1 = getB2()", B.B2)
			.addTest("b1 = a1", A.A1)
			.addTest("b1 = getA1()", A.A1)
			.addTest("b1 = b1", B.B1)
			.addTest("b1 = getB1()", B.B1)
			.addTest("b1 = b2", B.B2)
			.addTest("b1 = getB2()", B.B2)
			.addTest("b2 = b1", B.B1)			// works because runtime type of b1 is B
			.addTest("b2 = getB1()", B.B1)	// works because now also runtime type of result of getB1() is evaluated
			.addTest("b2 = b2", B.B2)
			.addTest("b2 = getB2()", B.B2)
			.addTestWithError("b2 = a1", SemanticException.class)
			.addTestWithError("b2 = getA1()", SemanticException.class);

		return builder.build();
	}

	private static class TestClass
	{
		private double 	d = 3.0;
		private float 	f = 2.f;
		private int		i = 5;

		private A a1 = A.A1;
		private A b1 = B.B1;
		private B b2 = B.B2;

		A getA1() {
			return a1;
		}

		A getB1() {
			return b1;
		}

		B getB2() {
			return b2;
		}

		TestClass get(Object dummy) {
			return this;
		}
	}

	private static class A {
		static final A A1 = new A();
	}

	private static class B extends A {
		static final B B1	= new B();
		static final B B2	= new B();
	}
}
