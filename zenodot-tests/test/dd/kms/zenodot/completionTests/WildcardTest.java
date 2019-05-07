package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.common.CustomHierarchy;
import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import dd.kms.zenodot.settings.ParserSettingsUtils;
import dd.kms.zenodot.settings.Variable;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class WildcardTest extends CompletionTest
{
	public WildcardTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new TestClass();
		Variable variable1 = ParserSettingsUtils.createVariable("tempFloatVariable", 13.5f, true);
		Variable variable2 = ParserSettingsUtils.createVariable("tempCharVariable", 'c', true);
		return new CompletionTestBuilder()
			.testInstance(testInstance)
			.configurator(test -> {
				test.importPackages("java.util");
				test.variables(variable1, variable2);
				test.customHierarchyRoot(CustomHierarchy.ROOT);
			})
			.addTest("xY", 								"xYZ", "xyz", "xxYyZz")
			.addTest("xYZ",								"xYZ", "xyz", "xxYyZz")
			.addUnstableTest("ArLi",					"ArrayList")
			.addUnstableTest("LHS",						"LinkedHashSet")
			.addTest("gVA",								"getValueAsDouble()", "getValueAsInt()")
			.addTest("geValA",							"getValueAsDouble()", "getValueAsInt()")
			.addTest("gVAD",							"getValueAsDouble()")
			.addTest("gVAI",							"getValueAsInt()")
			.addTest("tFV",								"tempFloatVariable")
			.addTest("tCV",								"tempCharVariable")
			.addTest("{CM",								"Component Manager")
			.addTest("{Productivity Calculation#RP",	"Relative Productivity", "Relative Productivity Potential")
			.build();
	}

	private static class TestClass
	{
		private int xxyyzz;
		private int xyz;
		private int xYZ;
		private int xxYyZz;

		double getValueAsDouble() { return 0.0; }
		int getValueAsInt() { return 0; }
	}
}
