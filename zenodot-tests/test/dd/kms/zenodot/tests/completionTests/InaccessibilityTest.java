package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTest;
import dd.kms.zenodot.tests.completionTests.framework.CompletionTestBuilder;
import dd.kms.zenodot.tests.completionTests.framework.TestData;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.swing.*;
import java.util.Collection;

@RunWith(Parameterized.class)
public class InaccessibilityTest extends CompletionTest
{
	public InaccessibilityTest(TestData testData) {
		super(testData);
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object> getTestData() {
		Object testInstance = new JTree();
		CompletionTestBuilder testBuilder = new CompletionTestBuilder().testInstance(testInstance);

		testBuilder
			.configurator(test -> {
				test.evaluationMode(EvaluationMode.MIXED);
			})
			/*
			 * The following test encounters the field JTree.$assertionsDisabled during completion.
			 * This field cannot be made accessible in Java 17+. This must not lead to an exception.
			 */
			.addTest("");

		return testBuilder.build();
	}
}
