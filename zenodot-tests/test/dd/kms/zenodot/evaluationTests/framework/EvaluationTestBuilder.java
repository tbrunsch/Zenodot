package dd.kms.zenodot.evaluationTests.framework;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.common.AbstractTest;

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

	public EvaluationTestBuilder addTest(String javaExpression, Object expectedValue) {
		return addTest(new SuccessfulEvaluation(javaExpression, expectedValue));
	}

	public EvaluationTestBuilder addUnstableTest(String javaExpression, Object expectedValue) {
		if (!AbstractTest.SKIP_UNSTABLE_TESTS) {
			addTest(javaExpression, expectedValue);
		}
		return this;
	}

	public EvaluationTestBuilder addTestWithError(String javaExpression) {
		return addTest(new EvaluationTestWithError(javaExpression));
	}

	private EvaluationTestBuilder addTest(TestExecutor testExecutor) {
		testDataBuilder.add(new TestData(testInstance, testConfigurator, testExecutor));
		return this;
	}

	public List<Object> build() {
		return testDataBuilder.build();
	}
}
