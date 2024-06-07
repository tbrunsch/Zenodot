package dd.kms.zenodot.tests.completionTests;

import dd.kms.zenodot.api.ParseException;
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
				test.assumeMinimumJavaMajorVersion(17);
				test.evaluationMode(EvaluationMode.MIXED);
			})
			.addTest("");	// encounters JTree.$assertionsDisabled during completion, which is inaccessible

		return testBuilder.build();
	}
}
