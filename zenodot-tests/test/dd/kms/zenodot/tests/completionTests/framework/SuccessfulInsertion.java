package dd.kms.zenodot.tests.completionTests.framework;

class SuccessfulInsertion implements TestExecutor
{
	private final String	expression;
	private final int		caretPosition;
	private final String	expectedResult;

	SuccessfulInsertion(String expression, int caretPosition, String expectedResult) {
		this.expression = expression;
		this.caretPosition = caretPosition;
		this.expectedResult = expectedResult;
	}

	@Override
	public void executeTest(CompletionTest test) {
		test.testInsertion(expression, caretPosition, expectedResult);
	}

	@Override
	public String toString() {
		return expression.substring(0, caretPosition)
			+ "^"
			+ expression.substring(caretPosition);
	}
}
