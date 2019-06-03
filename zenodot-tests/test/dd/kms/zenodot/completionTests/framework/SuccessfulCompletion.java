package dd.kms.zenodot.completionTests.framework;

class SuccessfulCompletion implements TestExecutor
{
	private final String	expression;
	private final String[]	expectedSuggestions;

	SuccessfulCompletion(String expression, String[] expectedSuggestions) {
		this.expression = expression;
		this.expectedSuggestions = expectedSuggestions;
	}

	@Override
	public void executeTest(CompletionTest test) {
		test.testCompletion(expression, expectedSuggestions);
	}

	@Override
	public String toString() {
		return expression;
	}
}
