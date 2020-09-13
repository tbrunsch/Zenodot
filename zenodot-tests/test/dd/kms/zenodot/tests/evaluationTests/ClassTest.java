package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.api.common.AccessModifier;
import dd.kms.zenodot.impl.utils.ClassUtils;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.Collections;

@RunWith(Parameterized.class)
public class ClassTest extends EvaluationTest
{
	public ClassTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		String className = TestClass.class.getName().replace('$', '.');
		String packageName = ClassUtils.getParentPath(ClassTest.class.getPackage().getName()) + ".classesForTest";
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder();

		testBuilder
			.configurator(test -> test.minimumAccessModifier(AccessModifier.PACKAGE_PRIVATE))
			.addTest(className + ".l",				-17L)
			.addTest(className + ".f",				27.5f)
			.addTest(className + ".getInt()",		0)
			.addTest(className + ".getDouble()",	2.0);

		testBuilder
			.configurator(test -> {
				test.minimumAccessModifier(AccessModifier.PUBLIC);
				test.importPackages("java.util");
			})
			.addTest("Math.pow(1.5, 2.5)",		Math.pow(1.5, 2.5))
			.addTest("Math.PI", 				Math.PI)
			.addTest("Collections.emptyList()", Collections.emptyList());

		testBuilder
			.configurator(null)
			.addTest(packageName + ".dummies.MyClass.VALUE", 				5)
			.addTest(packageName + ".dummies.MyOtherClass.OTHER_VALUE", 	7.5)
			.addTest(packageName + ".moreDummies.MyDummy.FIRST_DUMMY", 		true);

		testBuilder
			.configurator(test -> test.importPackages(packageName))
			.addTest("DummyClass.FIRST_CHARACTER",		'D')
			.addTest("MyDummyClass.FIRST_CHARACTER",	'M');

		testBuilder
			.configurator(test -> test.importClasses(packageName + ".dummies.YetAnotherDummyClass"))
			.addTest("YetAnotherDummyClass.NAME",	"YetAnotherDummyClass");

		testBuilder
			.configurator(test -> test.importClasses(packageName + ".DummyClass.InternalClassStage1"))
			.addTest("InternalClassStage1.value",					5.0)
			.addTest("InternalClassStage1.InternalClassStage2.i",	3);

		testBuilder
			.configurator(test -> test.importClasses(packageName + ".DummyClass.InternalClassStage1.InternalClassStage2"))
			.addTest("InternalClassStage2.i",	3);

		testBuilder
			.configurator(test -> test.minimumAccessModifier(AccessModifier.PACKAGE_PRIVATE))
			.addTestWithError(className + ".i")
			.addTestWithError(className + ".b")
			.addTestWithError(className + ".d")
			.addTestWithError(className + ".getLong()")
			.addTestWithError(className + ".getString()")
			.addTestWithError(className + ".getFloat()");

		testBuilder
			.configurator(test -> test.importPackages(packageName + ".dummies"))
			.addTestWithError("DummyClass.FIRST_CHARACTER")
			.addTestWithError("MyDummy2.FIRST_DUMMY");

		testBuilder
			.configurator(test -> test.importClasses(packageName + ".dummies.MyClass"))
			.addTestWithError("MyOtherClass.OTHER_VALUE");

		return testBuilder.build();
	}

	private static class TestClass
	{
		int i = 23;
		static long l = -17L;
		private static byte b = (byte) 25;
		double d = 1.3;
		static float f = 27.5f;

		static int getInt() { return 0; }
		long getLong() { return 1L; }
		private static String getString() { return "abc"; }
		static double getDouble() { return 2.0; }
		float getFloat() { return 3.0f; }
	}
}
