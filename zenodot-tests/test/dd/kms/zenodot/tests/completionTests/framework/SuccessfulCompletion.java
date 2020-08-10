package dd.kms.zenodot.tests.completionTests.framework;

class SuccessfulCompletion implements TestExecutor
{
	private final String	expression;
	private final String[]	expectedCompletions;

	SuccessfulCompletion(String expression, String[] expectedCompletions) {
		this.expression = expression;
		this.expectedCompletions = expectedCompletions;
	}

	@Override
	public void executeTest(CompletionTest test) {
		test.testCompletion(expression, expectedCompletions);
	}

	@Override
	public String toString() {
		return expression;
	}
}
