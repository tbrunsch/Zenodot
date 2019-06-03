package dd.kms.zenodot.evaluationTests.framework;

class EvaluationTestWithError implements TestExecutor
{
	private final String						expression;

	EvaluationTestWithError(String expression) {
		this.expression = expression;
	}

	@Override
	public void executeTest(EvaluationTest test) {
		test.testEvaluationWithError(expression);
	}

	@Override
	public String toString() {
		return expression;
	}
}
