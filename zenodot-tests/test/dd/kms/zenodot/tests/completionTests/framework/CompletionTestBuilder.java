package dd.kms.zenodot.tests.completionTests.framework;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.tests.common.AbstractTest;

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

	public CompletionTestBuilder addTest(String expression, String... expectedCompletions) {
		return addTest(new SuccessfulCompletion(expression, expectedCompletions));
	}

	public CompletionTestBuilder addUnstableTest(String expression, String... expectedCompletions) {
		if (!AbstractTest.SKIP_UNSTABLE_TESTS) {
			addTest(expression, expectedCompletions);
		}
		return this;
	}

	public CompletionTestBuilder addInsertionTest(String expressionWithMarkedCaret, String expectedResult) {
		int caretPosition = expressionWithMarkedCaret.indexOf('@');
		if (caretPosition < 0) {
			throw new IllegalArgumentException("The caret must be marked with '@' within the expression");
		}
		String expression = expressionWithMarkedCaret.substring(0, caretPosition)
			+ expressionWithMarkedCaret.substring(caretPosition + 1);
		return addInsertionTest(expression, caretPosition, expectedResult);
	}

	public CompletionTestBuilder addInsertionTest(String expression, int caretPosition, String expectedResult) {
		return addTest(new SuccessfulInsertion(expression, caretPosition, expectedResult));
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
