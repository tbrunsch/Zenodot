package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.CustomHierarchyParsers;
import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.settings.parsers.AdditionalParserSettings;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class CustomHierarchyValidExpressionTest extends CompletionTest
{
	public CustomHierarchyValidExpressionTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		/*
		 * Test that even valid expressions are not evaluated in completion mode.
		 * (Evaluations will result in an exception, so we only test that no
		 * exception is thrown.)
		 */
		return new CompletionTestBuilder()
			.configurator(CustomHierarchyValidExpressionTest::configureTest)
			.addTest("{node}")
			.build();
	}

	private static void configureTest(CompletionTest test) {
		ObjectTreeNode node = CustomHierarchyParsers.createLeafNode("node", 123);
		ObjectTreeNode root = new ObjectTreeNode() {
			@Override
			public String getName() {
				return null;
			}

			@Override
			public Iterable<ObjectTreeNode> getChildNodes() {
				return Arrays.asList(node);
			}

			@Override
			public Object getUserObject() {
				return null;
			}
		};
		AdditionalParserSettings additionalParserSettings = CustomHierarchyParsers.createCustomHierarchyParserSettings(root);
		test.additionalParserSettings(additionalParserSettings);
	}
}
