package dd.kms.zenodot.tests.evaluationTests.framework;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.tests.common.AbstractTest;

import javax.annotation.Nullable;
import java.util.List;

public class EvaluationTestBuilder
{
	private final ImmutableList.Builder<Object>	testDataBuilder	= ImmutableList.builder();

	private Object						testInstance;
	private @Nullable TestConfigurator	testConfigurator;

	public EvaluationTestBuilder testInstance(Object testInstance) {
		this.testInstance = testInstance;
		return this;
	}

	public EvaluationTestBuilder configurator(@Nullable TestConfigurator testConfigurator) {
		this.testConfigurator = testConfigurator;
		return this;
	}

	public EvaluationTestBuilder addTest(String expression, Object expectedValue) {
		return addTest(new SuccessfulEvaluation(expression, expectedValue));
	}

	public EvaluationTestBuilder addUnstableTest(String expression, Object expectedValue) {
		if (!AbstractTest.SKIP_UNSTABLE_TESTS) {
			addTest(expression, expectedValue);
		}
		return this;
	}

	public EvaluationTestBuilder addTestWithError(String expression) {
		return addTest(new EvaluationTestWithError(expression));
	}

	private EvaluationTestBuilder addTest(TestExecutor testExecutor) {
		testDataBuilder.add(new TestData(testInstance, testConfigurator, testExecutor));
		return this;
	}

	public List<Object> build() {
		return testDataBuilder.build();
	}
}
