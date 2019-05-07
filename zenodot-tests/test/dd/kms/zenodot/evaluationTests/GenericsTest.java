package dd.kms.zenodot.evaluationTests;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@RunWith(Parameterized.class)
public class GenericsTest extends EvaluationTest
{
	public GenericsTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		TestClass testInstance = new TestClass();
		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTest("testCollInt(collListInt)",		testInstance.collListInt)
			.addTest("testCollInt(collSetInt)",			testInstance.collSetInt)
			.addTest("testCollInt(listInt)",			testInstance.listInt)
			.addTest("testCollString(collListString)",	testInstance.collListString)
			.addTest("testCollString(collSetString)",	testInstance.collSetString)
			.addTest("testCollString(listString)",		testInstance.listString)
			.addTest("testListInt(listInt)",			testInstance.listInt)
			.addTest("testListString(listString)",		testInstance.listString);

		testBuilder
			.configurator(null)
			.addTestWithError("testCollInt(collListString)")
			.addTestWithError("testCollInt(collSetString)")
			.addTestWithError("testCollInt(listString)")

			.addTestWithError("testCollString(collListInt)")
			.addTestWithError("testCollString(collSetInt)")
			.addTestWithError("testCollString(listInt)")

			.addTestWithError("testListInt(collListInt)")
			.addTestWithError("testListInt(collListString)")
			.addTestWithError("testListInt(collSetInt)")
			.addTestWithError("testListInt(collSetString)")
			.addTestWithError("testListInt(listString)")

			.addTestWithError("testListString(collListInt)")
			.addTestWithError("testListString(collListString)")
			.addTestWithError("testListString(collSetInt)")
			.addTestWithError("testListString(collSetString)")
			.addTestWithError("testListString(listInt)")

			.addTestWithError("testSetInt(collListInt)")
			.addTestWithError("testSetInt(collListString)")
			.addTestWithError("testSetInt(collSetInt)")
			.addTestWithError("testSetInt(collSetString)")
			.addTestWithError("testSetInt(listInt)")
			.addTestWithError("testSetInt(listString)")

			.addTestWithError("testSetString(collListInt)")
			.addTestWithError("testSetString(collListString)")
			.addTestWithError("testSetString(collSetInt)")
			.addTestWithError("testSetString(collSetString)")
			.addTestWithError("testSetString(listInt)")
			.addTestWithError("testSetString(listString)");

		testBuilder
			.configurator(test -> test.enableDynamicTyping())
			.addTest("testListInt(collListInt)",		testInstance.collListInt)
			.addTest("testListString(collListString)",	testInstance.collListString)
			.addTest("testSetInt(collSetInt)",			testInstance.collSetInt)
			.addTest("testSetString(collSetString)",	testInstance.collSetString);

		return testBuilder.build();
	}

	private static class TestClass
	{
		private final Collection<Integer>	collListInt		= Lists.newArrayList(1);
		private final Collection<String>	collListString	= Lists.newArrayList("2");
		private final Collection<Integer>	collSetInt		= Sets.newHashSet(3);
		private final Collection<String>	collSetString	= Sets.newHashSet("4");
		private final List<Integer> 		listInt			= Lists.newArrayList(5);
		private final List<String>			listString		= Lists.newArrayList("6");

		Collection<Integer>	testCollInt(Collection<Integer> c) { return c; }
		Collection<String>	testCollString(Collection<String> c) { return c; }
		List<Integer>		testListInt(List<Integer> l) { return l; }
		List<String>		testListString(List<String> l) { return l; }
		Set<Integer> 		testSetInt(Set<Integer> s) { return s; }
		Set<String>			testSetString(Set<String> s) { return s; }
	}
}
