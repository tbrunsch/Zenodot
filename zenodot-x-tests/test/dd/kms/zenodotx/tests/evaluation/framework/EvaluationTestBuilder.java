package dd.kms.zenodotx.tests.evaluation.framework;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodotx.tests.common.AbstractTest;
import dd.kms.zenodotx.tests.common.ResettableTestClass;

import javax.annotation.Nullable;
import java.util.List;

public class EvaluationTestBuilder
{
	private final ImmutableList.Builder<Object>	testDataBuilder	= ImmutableList.builder();

	private Object						testInstance		= null;
	private @Nullable TestConfigurator	testConfigurator;

	/**
	 * @param testInstance The instance the literal {@code this} points to during test execution.
	 *                     If the class if {@code testInstance} implements {@link ResettableTestClass},
	 *                     then the method {@link ResettableTestClass#reset()} is automatically called
	 *                     after adding a test and after test execution to ensure that the test instance
	 *                     is in its initial state at the beginning of each test. This is required for
	 *                     tests with side effects that shall be executed independently of each other,
	 *                     i.e., without relying on the side effect of the previous test.
	 */
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

	public EvaluationTestBuilder addTestWithError(String expression, Class<? extends Exception> expectedExceptionClass) {
		return addTest(new EvaluationTestWithError(expression, expectedExceptionClass));
	}

	private EvaluationTestBuilder addTest(TestExecutor testExecutor) {
		testDataBuilder.add(new TestData(testInstance, testConfigurator, testExecutor));
		if (testInstance instanceof ResettableTestClass) {
			((ResettableTestClass) testInstance).reset();
		}
		return this;
	}

	public List<Object> build() {
		return testDataBuilder.build();
	}
}
