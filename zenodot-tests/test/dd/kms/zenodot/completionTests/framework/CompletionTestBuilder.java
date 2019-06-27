package dd.kms.zenodot.completionTests.framework;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.common.AbstractTest;

import javax.annotation.Nullable;
import java.util.List;

public class CompletionTestBuilder
{
	private final ImmutableList.Builder<Object>	testDataBuilder	= ImmutableList.builder();

	private Object						testInstance;
	private @Nullable TestConfigurator	testConfigurator;

	public CompletionTestBuilder testInstance(Object testInstance) {
		this.testInstance = testInstance;
		return this;
	}

	public CompletionTestBuilder configurator(@Nullable TestConfigurator testConfigurator) {
		this.testConfigurator = testConfigurator;
		return this;
	}

	public CompletionTestBuilder addTest(String expression, String... expectedSuggestions) {
		return addTest(new SuccessfulCompletion(expression, expectedSuggestions));
	}

	public CompletionTestBuilder addUnstableTest(String expression, String... expectedSuggestions) {
		if (!AbstractTest.SKIP_UNSTABLE_TESTS) {
			addTest(expression, expectedSuggestions);
		}
		return this;
	}

	public CompletionTestBuilder addTestWithError(String expression, Class<? extends Exception> expectedExceptionClass) {
		return addTestWithError(expression, expression.length(), expectedExceptionClass);
	}

	public CompletionTestBuilder addTestWithError(String expression, int caretPosition, Class<? extends Exception> expectedExceptionClass) {
		return addTest(new CompletionWithError(expression, caretPosition, expectedExceptionClass));
	}

	private CompletionTestBuilder addTest(TestExecutor testExecutor) {
		testDataBuilder.add(new TestData(testInstance, testConfigurator, testExecutor));
		return this;
	}

	public List<Object> build() {
		return testDataBuilder.build();
	}
}