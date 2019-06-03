package dd.kms.zenodot.evaluationTests.framework;

class SuccessfulEvaluation implements TestExecutor
{
	private final String	expression;
	private final Object	expectedValue;

	SuccessfulEvaluation(String expression, Object expectedValue) {
		this.expression = expression;
		this.expectedValue = expectedValue;
	}

	@Override
	public void executeTest(EvaluationTest test) {
		test.testEvaluation(expression, expectedValue);
	}

	@Override
	public String toString() {
		return expression;
	}
}
