package dd.kms.zenodot.tests.evaluationTests.framework;

class EvaluationTestWithError implements TestExecutor
{
	private final String expression;

	EvaluationTestWithError(String expression) {
		this.expression = expression;
	}

	@Override
	public void executeTest(EvaluationTest test, boolean compile) {
		test.testEvaluationWithError(expression, compile);
	}

	@Override
	public String toString() {
		return expression;
	}
}
