package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class ObjectConstructorTest extends CompletionTest
{
	public ObjectConstructorTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass(5, -2.0f);
		return new CompletionTestBuilder()
			.testInstance(testInstance)
			.addTest("new TestC",						"TestClass")
			.addTest("new TestClass(",					"i", "o")
			.addTest("new TestClass(i, ",				"i")
			.addTest("new TestClass(o, ",				"i")
			.addTest("new TestClass(\"bla\", ",			"d", "i")
			.addTest("new TestClass(\"bla\", i, ",		"i")
			.addTest("new TestClass(\"bla\", d, i).",	"d", "i", "o")
			.build();
	}

	private static class TestClass
	{
		private final int i;
		private final double d;
		private final Object o;

		TestClass(int i, float f) {
			this.i = i;
			this.d = f;
			o = this;
		}

		TestClass(String s, double d, int i) {
			this.i = i;
			this.d = d;
			this.o = s;
		}

		TestClass(Object o, int i) {
			this.i = i;
			this.d = 0.0;
			this.o = o;
		}
	}
}
