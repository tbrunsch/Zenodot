package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class SpacesTest extends EvaluationTest
{
	public SpacesTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass("abc", (short) 13, 'X', 123456789, -13e02f, 1L, false, 2.34e-56);
		return new EvaluationTestBuilder()
			.testInstance(testInstance)
			.addTest(" s ", "abc")
			.addTest("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ) . sValue ",	(short) 13)
			.addTest("  getTestClass (  \"xyz\" , sValue,  c ,i  ,  f , l,b,d  ) .s",	"xyz" + "_xyz")
			.addTest("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ). c",			'X')
			.addTest("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d  ).i ",			123456789 + 1)
			.addTest("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d) .f",			-13e02f / 2.f)
			.addTest("  getTestClass (  s , sValue,  c ,i  ,  f , l,b,d ). l",			1L * 3)
			.addTest("  getTestClass (s,sValue,  c,i  ,  f,l,b,d ).b",					!false)
			.addTest("  getTestClass( s, sValue, c, i, f, l, b, d  ) . d",				3 - 2.34e-56)
			.addTest("  \"abc\"",														"abc")
			.build();
	}

	private static class TestClass
	{
		private final String s;
		private final short sValue;
		private final char c;
		private final int i;
		private final float f;
		private final long l;
		private final boolean b;
		private final double d;

		TestClass(String s, short sValue, char c, int i, float f, long l, boolean b, double d) {
			this.s = s;
			this.sValue = sValue;
			this.c = c;
			this.i = i;
			this.f = f;
			this.l = l;
			this.b = b;
			this.d = d;
		}

		TestClass getTestClass(String s, short sValue, char c, int i, float f, long l, boolean b, double d) {
			return new TestClass(s + "_xyz", sValue, c, i + 1, f / 2.f, l * 3, !b, 3 - d);
		}
	}
}
