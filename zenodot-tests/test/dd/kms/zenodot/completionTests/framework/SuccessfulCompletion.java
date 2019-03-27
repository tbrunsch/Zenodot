package dd.kms.zenodot.completionTests.framework;

class SuccessfulCompletion implements TestExecutor
{
	private final String	javaExpression;
	private final String[]	expectedSuggestions;

	SuccessfulCompletion(String javaExpression, String[] expectedSuggestions) {
		this.javaExpression = javaExpression;
		this.expectedSuggestions = expectedSuggestions;
	}

	@Override
	public void executeTest(CompletionTest test) {
		test.testCompletion(javaExpression, expectedSuggestions);
	}

	@Override
	public String toString() {
		return javaExpression;
	}
}
