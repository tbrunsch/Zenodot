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

	public CompletionTestBuilder addTest(String javaExpression, String... expectedSuggestions) {
		return addTest(new SuccessfulCompletion(javaExpression, expectedSuggestions));
	}

	public CompletionTestBuilder addUnstableTest(String javaExpression, String... expectedSuggestions) {
		if (!AbstractTest.SKIP_UNSTABLE_TESTS) {
			addTest(javaExpression, expectedSuggestions);
		}
		return this;
	}

	public CompletionTestBuilder addTestWithError(String javaExpression, Class<? extends Exception> expectedExceptionClass) {
		return addTestWithError(javaExpression, javaExpression.length(), expectedExceptionClass);
	}

	public CompletionTestBuilder addTestWithError(String javaExpression, int caret, Class<? extends Exception> expectedExceptionClass) {
		return addTest(new CompletionWithError(javaExpression, caret, expectedExceptionClass));
	}

	private CompletionTestBuilder addTest(TestExecutor testExecutor) {
		testDataBuilder.add(new TestData(testInstance, testConfigurator, testExecutor));
		return this;
	}

	public List<Object> build() {
		return testDataBuilder.build();
	}
}
