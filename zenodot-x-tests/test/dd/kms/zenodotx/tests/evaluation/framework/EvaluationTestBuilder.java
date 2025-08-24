package dd.kms.zenodotx.tests.evaluation.framework;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodotx.tests.common.AbstractTest;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class EvaluationTestBuilder
{
	private final ImmutableList.Builder<Object>	testDataBuilder	= ImmutableList.builder();

	private Supplier<Object>			testInstanceProvider	= () -> null;
	private @Nullable TestConfigurator	testConfigurator;

	public EvaluationTestBuilder testInstanceProvider(Supplier<Object> testInstanceProvider) {
		this.testInstanceProvider = testInstanceProvider;
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

	public EvaluationTestBuilder addTestWithError(String expression, Class<? extends Exception> expectedExceptionClass) {
		return addTest(new EvaluationTestWithError(expression, expectedExceptionClass));
	}

	private EvaluationTestBuilder addTest(TestExecutor testExecutor) {
		testDataBuilder.add(new TestData(testInstanceProvider, testConfigurator, testExecutor));
		return this;
	}

	public List<Object> build() {
		return testDataBuilder.build();
	}
}
