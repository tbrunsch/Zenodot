package dd.kms.zenodot.tests.completionTests.framework;

class CompletionWithBlacklist implements TestExecutor
{
	private final String	expression;
	private final String[]	unexpectedCompletions;

	CompletionWithBlacklist(String expression, String[] unexpectedCompletions) {
		this.expression = expression;
		this.unexpectedCompletions = unexpectedCompletions;
	}

	@Override
	public void executeTest(CompletionTest test) {
		test.testCompletionWithBlacklist(expression, unexpectedCompletions);
	}

	@Override
	public String toString() {
		return expression;
	}
}
