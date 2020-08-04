package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ObjectConstructorTest extends EvaluationTest
{
	public ObjectConstructorTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass(0, 0.0f);
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(test -> test.minimumAccessLevel(AccessModifier.PACKAGE_PRIVATE))
			.addTest("new TestClass(5, 6.0f).i",										5)
			.addTest("new TestClass(5, 6.0f).l",										1L)
			.addTest("new TestClass(5, 6.0f).f",										6.0f)
			.addTest("new TestClass(5, 6.0f).d",										2.0)
			.addTest("new TestClass(7.0, 8L).i",										3)
			.addTest("new TestClass(7.0, 8L).l",										8L)
			.addTest("new TestClass(7.0, 8L).f",										4.0f)
			.addTest("new TestClass(7.0, 8L).d",										7.0)
			.addTest("new TestClass(9, 10L, 11.f, 12.0).i",								9)
			.addTest("new TestClass(9, 10L, 11.f, 12.0).l",								10L)
			.addTest("new TestClass(9, 10L, 11.f, 12.0).f",								11.f)
			.addTest("new TestClass(9, 10L, 11.f, 12.0).d",								12.0)
			.addTest("new TestClass(0, 0, 0, 0).i",										0)
			.addTest("new TestClass(0, 0, 0, 0).l",										0L)
			.addTest("new TestClass(0, 0, 0, 0).f",										0.f)
			.addTest("new TestClass(0, 0, 0, 0).d",										0.0)
			.addTest("new StringBuilder(\"Test\").append('X').append(13).toString()",	"TestX13");

		testBuilder
			.configurator(test -> test.minimumAccessLevel(AccessModifier.PACKAGE_PRIVATE))
			.addTestWithError("new TestClass(0)")
			.addTestWithError("new TestClass(0, 0)")
			.addTestWithError("new TestClass(0, 0, 0)")
			.addTestWithError("new TestClass(0, 0, 0, 0, 0)");

		return testBuilder.build();
	}

	private static class TestClass
	{
		final int i;
		final long l;
		final float f;
		final double d;

		TestClass(int i, float f) {
			this.i = i;
			this.l = 1L;
			this.f = f;
			this.d = 2.0;
		}

		TestClass(double d, long l) {
			this.i = 3;
			this.l = l;
			this.f = 4.0f;
			this.d = d;
		}

		TestClass(int i, long l, float f, double d) {
			this.i = i;
			this.l = l;
			this.f = f;
			this.d = d;
		}
	}
}
