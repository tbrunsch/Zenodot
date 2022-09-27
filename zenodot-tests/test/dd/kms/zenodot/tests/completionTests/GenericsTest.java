package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

@RunWith(Parameterized.class)
public class GenericsTest extends CompletionTest
{
	public GenericsTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTest("testCollInt(",		"collListInt", "collSetInt", "listInt")
			.addTest("testCollString(",		"collListString", "collSetString", "listString")
			.addTest("testListInt(",		"listInt")
			.addTest("testListString(",		"listString")
			.addTest("testSetInt(",			"collListInt", "collListString", "collSetInt", "collSetString", "listInt", "listString")		// no class match, so fields ordered lexicographically
			.addTest("testSetString(",		"collListInt", "collListString", "collSetInt", "collSetString", "listInt", "listString");	// no class match, so fields ordered lexicographically

		testBuilder
			.configurator(test -> test.evaluationMode(EvaluationMode.MIXED))
			.addTest("testCollInt(",		"collListInt", "collSetInt", "listInt")
			.addTest("testCollString(",		"collListString", "collSetString", "listString")
			.addTest("testListInt(",		"collListInt", "listInt")
			.addTest("testListString(",		"collListString", "listString")
			.addTest("testSetInt(",			"collSetInt")
			.addTest("testSetString(",		"collSetString");

		return testBuilder.build();
	}

	private static class TestClass
	{
		private Collection<Integer> collListInt = new ArrayList<>();
		private Collection<String> collListString = new ArrayList<>();
		private Collection<Integer> collSetInt = new HashSet<>();
		private Collection<String> collSetString = new HashSet<>();
		private List<Integer> listInt = new ArrayList<>();
		private List<String> listString = new ArrayList<>();

		void testCollInt(Collection<Integer> c) {}
		void testCollString(Collection<String> c) {}
		void testListInt(List<Integer> l) {}
		void testListString(List<String> l) {}
		void testSetInt(Set<Integer> s) {}
		void testSetString(Set<String> s) {}
	}
}
