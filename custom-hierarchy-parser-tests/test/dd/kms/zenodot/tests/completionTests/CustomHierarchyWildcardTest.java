package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.CustomHierarchyParserExtension;
import dd.kms.zenodot.tests.common.CustomHierarchy;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public class CustomHierarchyWildcardTest extends CompletionTest
{
	public CustomHierarchyWildcardTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		CustomHierarchyParserExtension customHierarchyParserExtension = CustomHierarchyParserExtension.create(CustomHierarchy.ROOT);

		return new CompletionTestBuilder()
			.configurator(test -> customHierarchyParserExtension.configure(test.getSettingsBuilder()))
			.addTest("{CM",								"Component Manager")
			.addTest("{Productivity Calculation#RP",	"Relative Productivity", "Relative Productivity Potential")
			.build();
	}
}
