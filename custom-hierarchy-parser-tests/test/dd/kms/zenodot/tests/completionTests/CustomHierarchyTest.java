package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.CustomHierarchyParsers;
import dd.kms.zenodot.api.settings.parsers.AdditionalParserSettings;
import dd.kms.zenodot.tests.common.CustomHierarchy;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class CustomHierarchyTest extends CompletionTest
{
	public CustomHierarchyTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		AdditionalParserSettings customHierarchyParserSettings = CustomHierarchyParsers.createCustomHierarchyParserSettings(CustomHierarchy.ROOT);

		return new CompletionTestBuilder()
			.configurator(test -> test.additionalParserSettings(customHierarchyParserSettings))
			.addTest("{Component Ma",											"Component Manager")
			.addTest("{Component Manager}.comp",								"components")
			.addTest("{Excel Imp",												"Excel Importer")
			.addTest("{Excel Importer}.comp",									"componentType")
			.addTest("{Excel Importer#A",										"Activity")
			.addTest("{Excel Importer#Activity}.data",							"dataType")
			.addTest("{Productivity Calculation}.data",							"dataItems")
			.addTest("{Productivity Calculation#Relative Prod",					"Relative Productivity", "Relative Productivity Potential")
			.addTest("{Productivity Calculation#Total Productivity (h)}.val",	"value")
			.build();
	}
}
