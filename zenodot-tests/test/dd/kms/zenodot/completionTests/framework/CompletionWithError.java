package dd.kms.zenodot.completionTests.framework;

class CompletionWithError implements TestExecutor
{
	private final String						javaExpression;
	private final int							caretPosition;
	private final Class<? extends Exception>	expectedExceptionClass;

	CompletionWithError(String javaExpression, int caretPosition, Class<? extends Exception> expectedExceptionClass) {
		this.javaExpression = javaExpression;
		this.caretPosition = caretPosition;
		this.expectedExceptionClass = expectedExceptionClass;
	}

	@Override
	public void executeTest(CompletionTest test) {
		test.testCompletionWithError(javaExpression, caretPosition, expectedExceptionClass);
	}

	@Override
	public String toString() {
		return javaExpression;
	}
}
