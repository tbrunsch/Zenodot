package dd.kms.zenodot.completionTests;

import dd.kms.zenodot.completionTests.framework.CompletionTest;
import dd.kms.zenodot.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.completionTests.framework.TestData;
import dd.kms.zenodot.settings.LeafObjectTreeNode;
import dd.kms.zenodot.settings.ObjectTreeNode;
import dd.kms.zenodot.settings.Variable;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ValidExpressionTest extends CompletionTest
{
	public ValidExpressionTest(TestData testData) {
		super(testData);
	}

	@Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		/*
		 * Test that even valid expressions are not evaluated in completion mode.
		 * (Evaluations will result in an exception, so we only test that no
		 * exception is thrown.)
		 */

		Object testInstance = new TestClass();
		return new CompletionTestBuilder()
			.testInstance(testInstance)
			.configurator(ValidExpressionTest::configureTest)
			.addTest("field")
			.addTest("method()")
			.addTest("variable")
			.addTest("TestClass")
			.addTest("new TestClass()")
			.addTest("new int[3]")
			.addTest("new int[]{ 1, 2, 3 }")
			.addTest("null")
			.addTest("5+3")
			.addTest("!false")
			.addTest("(1 << 2)")
			.addTest("{node}")
			.build();
	}

	private static void configureTest(CompletionTest test) {
		Variable variable = new Variable("variable", 13.0, true);

		ObjectTreeNode node = new LeafObjectTreeNode("node", 123);
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
			public @Nullable Object getUserObject() {
				return null;
			}
		};

		test.variables(variable);
		test.customHierarchyRoot(root);
	}

	private static class TestClass
	{
		private int field;

		void method() {}
	}
}
