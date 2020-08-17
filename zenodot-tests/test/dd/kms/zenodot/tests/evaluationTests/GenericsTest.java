package dd.kms.zenodot.tests.evaluationTests;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
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
			.addTest("testCollInt(collListInt)",		testInstance.collListInt)
			.addTest("testCollInt(collSetInt)",			testInstance.collSetInt)
			.addTest("testCollInt(listInt)",			testInstance.listInt)
			.addTest("testCollString(collListString)",	testInstance.collListString)
			.addTest("testCollString(collSetString)",	testInstance.collSetString)
			.addTest("testCollString(listString)",		testInstance.listString)
			.addTest("testListInt(listInt)",			testInstance.listInt)
			.addTest("testListString(listString)",		testInstance.listString);

		testBuilder
			.configurator(test -> test.importPackages("java.util"))
			.addTest("Arrays.asList(\"a\", \"b\", \"c\")", Arrays.asList("a", "b", "c"))
			.addTest("Arrays.asList(1, 2, 3).get(1)", 2)
			.addTest("Arrays.asList('a', \"b\", true, (byte) 3, (short) 4, 5, 6L, 7.f, 8.d).size()", 9);

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
