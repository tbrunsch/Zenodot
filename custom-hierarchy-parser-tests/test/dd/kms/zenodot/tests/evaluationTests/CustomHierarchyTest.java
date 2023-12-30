package dd.kms.zenodot.tests.evaluationTests;

import dd.kms.zenodot.api.CustomHierarchyParserExtension;
import dd.kms.zenodot.tests.common.CustomHierarchy;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTest;
import dd.kms.zenodot.tests.evaluationTests.framework.EvaluationTestBuilder;
import dd.kms.zenodot.tests.evaluationTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class CustomHierarchyTest extends EvaluationTest
{
	public CustomHierarchyTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		CustomHierarchyParserExtension customHierarchyParserExtension = CustomHierarchyParserExtension.create(CustomHierarchy.ROOT);

		EvaluationTestBuilder testBuilder = new EvaluationTestBuilder().configurator(test -> customHierarchyParserExtension.configure(test.getSettingsBuilder()));

		testBuilder
			.addTest("{Component Manager}",											CustomHierarchy.COMPONENT_MANAGER)
			.addTest("{Component Manager}.components.get(0)",						CustomHierarchy.COMPONENT_MANAGER.getComponents().get(0))
			.addTest("{Excel Importer}",											CustomHierarchy.EXCEL_IMPORTER)
			.addTest("{Excel Importer}.componentType",								CustomHierarchy.EXCEL_IMPORTER.getComponentType())
			.addTest("{Excel Importer#Activity}",									CustomHierarchy.ACTIVITY)
			.addTest("{Excel Importer#Activity}.dataType",							CustomHierarchy.ACTIVITY.getDataType())
			.addTest("{Productivity Calculation}.dataItems.get(1)",					CustomHierarchy.PRODUCTIVITY_CALCULATION.getDataItems().get(1))
			.addTest("{Productivity Calculation#Relative Productivity Potential}",	CustomHierarchy.RELATIVE_PRODUCTIVITY_POTENTIAL)
			.addTest("{Productivity Calculation#Total Productivity (h)}.value",		CustomHierarchy.TOTAL_PRODUCTIVITY.getValue());

		testBuilder
			.addTestWithError("Component Manager")
			.addTestWithError("{Component Manager")
			.addTestWithError("{Component Management}")
			.addTestWithError("{Excel Importer#componentType}");

		return testBuilder.build();
	}
}
