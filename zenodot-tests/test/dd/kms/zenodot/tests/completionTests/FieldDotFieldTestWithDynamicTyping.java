package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class FieldDotFieldTestWithDynamicTyping extends CompletionTest
{
	public FieldDotFieldTestWithDynamicTyping(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(null)
			.addTest("member.",		"x", "xyz")
			.addTest("member.x",	"x", "xyz")
			.addTest("member.xy",	"xyz", "x")
			.addTest("member.xyz",	"xyz", "x")
			.addInsertionTest("mem^.xyz",	'^', "member.xyz");

		testBuilder
			.configurator(test -> test.evaluationMode(EvaluationMode.MIXED))
			.addTest("member.",		"xy", "x", "xyz")
			.addTest("member.x",	"x", "xy", "xyz")
			.addTest("member.xy",	"xy", "xyz", "x")
			.addTest("member.xyz",	"xyz", "xy", "x")
			.addInsertionTest("mem^.xyz",	'^', "member.xyz");

		return testBuilder.build();
	}

	private static class BaseClass
	{
		private int x;
		private int xyz;
	}

	private static class DescendantClass extends BaseClass
	{
		private int	xy;
	}

	private static class TestClass
	{
		private BaseClass member = new DescendantClass();
	}
}
