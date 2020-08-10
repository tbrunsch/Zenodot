package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ArrayConstructorTest extends EvaluationTest
{
	public ArrayConstructorTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.addTest("fill(new int[3])[0]",									2)
			.addTest("fill(new int[3])[1]",									1)
			.addTest("fill(new int[3])[2]",									0)
			.addTest("fill(new String[4])[0]",								"2")
			.addTest("fill(new String[4])[1]",								"1")
			.addTest("fill(new String[4])[2]",								"0")
			.addTest("fill(new String[4])[3]",								"-1")
			.addTest("get(new int[]{ 3, 1, 4 })[0]",						3)
			.addTest("get(new int[]{ 3, 1, 4 })[1]",						1)
			.addTest("get(new int[]{ 3, 1, 4 })[2]",						4)
			.addTest("get(new String[]{ \"only\", \"a\", \"test\" })[0]",	"only")
			.addTest("get(new String[]{ \"only\", \"a\", \"test\" })[1]",	"a")
			.addTest("get(new String[]{ \"only\", \"a\", \"test\" })[2]",	"test");

		testBuilder
			.addTestWithError("new int[-1]")
			.addTestWithError("new int[]{ 1.3 }")
			.addTestWithError("new String[]{ 1 }");

		return testBuilder.build();
	}

	private static class TestClass
	{
		int[] fill(int[] a) {
			for (int i = 0; i < a.length; i++) {
				a[i] = 2 - i;
			}
			return a;
		}

		String[] fill(String[] a) {
			for (int i = 0; i < a.length; i++) {
				a[i] = String.valueOf(2 - i);
			}
			return a;
		}

		int[] get(int[] a) {
			return a;
		}

		String[] get(String[] a) {
			return a;
		}
	}
}
