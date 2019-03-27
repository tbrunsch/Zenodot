package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import dd.kms.zenodot.utils.ClassUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ClassCastTest extends CompletionTest
{
	public ClassCastTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass(5, -2.0);
		String packageName = ClassCastTest.class.getPackage().getName();
		String subpackageName = ClassUtils.getLeafOfPath(packageName);
		String className = ClassCastTest.class.getSimpleName();
		String testClassName = TestClass.class.getSimpleName();

		String qualifiedSubpackageNamePrefix = packageName.substring(0, packageName.length() - subpackageName.length() / 2);
		String qualifiedClassNamePrefix = packageName + "." + className.substring(0, className.length() / 2);

		return new CompletionTestBuilder()
			.testInstance(testInstance)
			.addTest("get((" + testClassName + ") o).",			"d", "i", "o")
			.addTest("get((" + testClassName + ") this).",		"d", "i", "o")
			.addTest("get(this).",								"d", "i", "o")	// no cast required for this
			.addTest("(" + qualifiedSubpackageNamePrefix,		subpackageName)
			.addTest("(" + qualifiedClassNamePrefix,			className)
			.addTestWithError("get(o).", 						ParseException.class)
			.build();
	}

	private static class TestClass
	{
		private final int i;
		private final double d;
		private final Object o;

		TestClass(int i, double d) {
			this.i = i;
			this.d = d;
			o = this;
		}

		TestClass get(TestClass o) { return o; }
	}
}
