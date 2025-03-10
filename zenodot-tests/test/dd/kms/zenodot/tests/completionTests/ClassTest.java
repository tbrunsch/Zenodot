package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.impl.utils.ClassUtils;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
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
			.configurator(test -> {
				test.minimumFieldAccessModifier(AccessModifier.PACKAGE_PRIVATE);
				test.minimumMethodAccessModifier(AccessModifier.PACKAGE_PRIVATE);
			})
			.addTest(className + ".",				"f", "l", "getDouble()", "getInt()", "InnerClass")
			.addTest(className + ".I",				"InnerClass")
			.addTest(className + ".InnerClass.",	"test")
			.addTest("String.CASE_I",				"CASE_INSENSITIVE_ORDER")
			.addTest("String.val",					"valueOf()");

		String packageName = ClassUtils.getParentPath(ClassTest.class.getPackage().getName()) + ".classesForTest";
		testBuilder
			.addTest(packageName + ".du",						"dummies", "DummyClass", "MyDummyClass", "moreDummies")
			.addTest(packageName + ".Du",						"DummyClass", "dummies", "MyDummyClass", "moreDummies")
			.addTest(packageName + ".m",						"moreDummies")
			.addTest(packageName + ".dummies.MyC",				"MyClass")
			.addTest(packageName + ".dummies.MyO",				"MyOtherClass")
			.addTest(packageName + ".dummies.Y",				"YetAnotherDummyClass")
			.addTest(packageName + ".moreDummies.MyDummy",		"MyDummy", "MyDummy2")
			.addTest(packageName + ".moreDummies.MyDummy2",		"MyDummy2", "MyDummy")
			.addInsertionTest(packageName + ".moreDumm^.MyDummy2", '^',	packageName + ".moreDummies.MyDummy2");

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
			.configurator(test -> test.minimumFieldAccessModifier(AccessModifier.PUBLIC))
			.addUnstableTest("Ma",		"Math")
			.addUnstableTest("Math.p",	"pow(, )", "PI")
			.addUnstableTest("Math.P",	"PI", "pow(, )");

		testBuilder
			.configurator(test -> test.enableConsideringAllClassesForClassCompletions())
			.addTest("MyC",			"dd.kms.zenodot.tests.classesForTest.dummies.MyClass")
			.addTest("YADC",		"dd.kms.zenodot.tests.classesForTest.dummies.YetAnotherDummyClass")
			.addTest("DummCl",		"dd.kms.zenodot.tests.classesForTest.DummyClass")
			.addTest("IntClStage1",	"dd.kms.zenodot.tests.classesForTest.DummyClass.InternalClassStage1");

		testBuilder
			.addTest("java.ut", 					"util")
			.addTest("java.util.func", 				"function")
			.addTest("java.util.function.BiFun",	"BiFunction");

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
