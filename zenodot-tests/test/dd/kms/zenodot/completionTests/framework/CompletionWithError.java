package dd.kms.zenodot.completionTests.framework;

class CompletionWithError implements TestExecutor
{
	private final String						javaExpression;
	private final int							caret;
	private final Class<? extends Exception>	expectedExceptionClass;

	CompletionWithError(String javaExpression, int caret, Class<? extends Exception> expectedExceptionClass) {
		this.javaExpression = javaExpression;
		this.caret = caret;
		this.expectedExceptionClass = expectedExceptionClass;
	}

	@Override
	public void executeTest(CompletionTest test) {
		test.testCompletionWithError(javaExpression, caret, expectedExceptionClass);
	}

	@Override
	public String toString() {
		return javaExpression;
	}
}
