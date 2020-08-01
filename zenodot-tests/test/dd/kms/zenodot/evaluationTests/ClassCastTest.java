package dd.kms.zenodot.evaluationTests;

import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ClassCastTest extends EvaluationTest
{
	public ClassCastTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass(5, -2.0, "abc");
		String className = InfoProvider.createClassInfoUnchecked(TestClass.class.getName()).getUnqualifiedName();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("merge((" + className + ") o1).i",			18)
			.addTest("merge((" + className + ") o1).d",			-4.5)
			.addTest("((" + className + ") o1).merge(this).i",	18)
			.addTest("((" + className + ") o1).merge(this).d",	4.5)
			.addTest("getId(o1)",								1)
			.addTest("getId((" + className + ") o1)",			3)
			.addTest("getId(o2)",								1)
			.addUnstableTest("getId((java.lang.String) o2)",	2)
			.addTest("getId((String) o2)",						2)
			.addTest("(int) i",									5)
			.addTest("(double) d",								-2.0)
			.addTest("(int) d",									-2)
			.addTest("(int) 2.3",								2);

		testBuilder
			.addTestWithError("(" + className + ") o2")
			.addTestWithError("(String) o1");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final int i;
		private final double d;
		private final Object o1;
		private final Object o2;

		TestClass() {
			i = 13;
			d = 2.5;
			o1 = this;
			o2 = "xyz";
		}

		TestClass(int i, double d, String o2) {
			this.i = i;
			this.d = d;
			o1 = new TestClass();
			this.o2 = o2;
		}

		TestClass merge(TestClass o) { return new TestClass(i + o.i, d - o.d, (String) o2); }

		int getId(Object o) { return 1; }
		int getId(String s) { return 2; }
		int getId(TestClass o) { return 3; }
	}
}
