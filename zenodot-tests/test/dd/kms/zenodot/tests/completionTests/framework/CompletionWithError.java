package dd.kms.zenodot.tests.completionTests.framework;

class CompletionWithError implements TestExecutor
{
	private final String						expression;
	private final int							caretPosition;
	private final Class<? extends Exception>	expectedExceptionClass;

	CompletionWithError(String expression, int caretPosition, Class<? extends Exception> expectedExceptionClass) {
		this.expression = expression;
		this.caretPosition = caretPosition;
		this.expectedExceptionClass = expectedExceptionClass;
	}

	@Override
	public void executeTest(CompletionTest test) {
		test.testCompletionWithError(expression, caretPosition, expectedExceptionClass);
	}

	@Override
	public String toString() {
		return expression;
	}
}
