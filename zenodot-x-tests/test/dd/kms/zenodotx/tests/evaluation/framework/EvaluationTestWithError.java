package dd.kms.zenodotx.tests.evaluation.framework;

class EvaluationTestWithError implements TestExecutor
{
	private final String						expression;
	private final Class<? extends Exception>	expectedExceptionClass;

	EvaluationTestWithError(String expression, Class<? extends Exception> expectedExceptionClass) {
		this.expression = expression;
		this.expectedExceptionClass = expectedExceptionClass;
	}

	@Override
	public void executeTest(EvaluationTest test, boolean compile) {
		test.testEvaluationWithError(expression, expectedExceptionClass, compile);
	}

	@Override
	public String toString() {
		return expression;
	}
}
