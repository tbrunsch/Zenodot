package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import dd.kms.zenodot.settings.AccessLevel;
import dd.kms.zenodot.utils.ClassUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ClassTest extends CompletionTest
{
	public ClassTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		CompletionTestBuilder testBuilder = new CompletionTestBuilder();

		String className = ClassUtils.getRegularClassName(TestClass.class.getName());
		testBuilder
			.configurator(test -> test.minimumAccessLevel(AccessLevel.PACKAGE_PRIVATE))
			.addTest(className + ".",				"f", "l", "getDouble()", "getInt()", "InnerClass")
			.addTest(className + ".I",				"InnerClass")
			.addTest(className + ".InnerClass.",	"test")
			.addTest("String.CASE_I",				"CASE_INSENSITIVE_ORDER")
			.addTest("String.val",					"valueOf()");

		String packageName = ClassUtils.getParentPath(ClassTest.class.getPackage().getName()) + ".classesForTest";
		testBuilder
			.configurator(null)
			.addTest(packageName + ".du",						"dummies", "DummyClass", "MyDummyClass", "moreDummies")
			.addTest(packageName + ".Du",						"DummyClass", "dummies", "MyDummyClass", "moreDummies")
			.addTest(packageName + ".m",						"moreDummies")
			.addTest(packageName + ".dummies.MyC",				"MyClass")
			.addTest(packageName + ".dummies.MyO",				"MyOtherClass")
			.addTest(packageName + ".dummies.Y",				"YetAnotherDummyClass")
			.addTest(packageName + ".moreDummies.MyDummy",		"MyDummy", "MyDummy2")
			.addTest(packageName + ".moreDummies.MyDummy2",		"MyDummy2", "MyDummy");

		testBuilder
			.configurator(test -> test.importPackages(packageName))
			.addTest("Du", "DummyClass")
			.addTest("My", "MyDummyClass");

		testBuilder
			.configurator(test -> test.importPackages(packageName + ".dummies"))
			.addTest("MyC",	"MyClass")
			.addTest("Y",	"YetAnotherDummyClass");

		testBuilder
			.configurator(test -> test.importClasses(packageName + ".moreDummies.MyDummy2"))
			.addTest("MyDummy", "MyDummy2");

		testBuilder
			.configurator(test -> test.importClasses(packageName + ".DummyClass.InternalClassStage1"))
			.addTest("InternalC",									"InternalClassStage1")
			.addTest("InternalClassStage1.v",						"value")
			.addTest("InternalClassStage1.i",						"InternalClassStage2")
			.addTest("InternalClassStage1.InternalClassStage2.",	"i");

		testBuilder
			.configurator(test -> test.importClasses(packageName + ".DummyClass.InternalClassStage1.InternalClassStage2"))
			.addTest("InternalC",				"InternalClassStage2")
			.addTest("InternalClassStage2.",	"i");

		testBuilder
			.configurator(test -> test.minimumAccessLevel(AccessLevel.PUBLIC))
			.addUnstableTest("Ma",		"Math")
			.addUnstableTest("Math.p",	"pow(, )", "PI")
			.addUnstableTest("Math.P",	"PI", "pow(, )");

		return testBuilder.build();
	}

	private static class TestClass
	{
		int i;
		static long l;
		private static byte b;
		double d;
		static float f;

		static int getInt() { return 0; }
		long getLong() { return 1L; }
		private static String getString() { return "abc"; }
		static double getDouble() { return 2.0; }
		float getFloat() { return 3.0f; }

		static final class InnerClass
		{
			static final int test = 13;
		}
	}
}
