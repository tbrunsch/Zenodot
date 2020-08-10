package dd.kms.zenodot.tests.evaluationTests.framework;

class SuccessfulEvaluation implements TestExecutor
{
	private final String	expression;
	private final Object	expectedValue;

	SuccessfulEvaluation(String expression, Object expectedValue) {
		this.expression = expression;
		this.expectedValue = expectedValue;
	}

	@Override
	public void executeTest(EvaluationTest test, boolean compile) {
		test.testEvaluation(expression, expectedValue, compile);
	}

	@Override
	public String toString() {
		return expression;
	}
}
