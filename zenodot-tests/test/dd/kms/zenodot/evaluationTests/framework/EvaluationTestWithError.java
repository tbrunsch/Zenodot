package dd.kms.zenodot.evaluationTests.framework;

class EvaluationTestWithError implements TestExecutor
{
	private final String						javaExpression;

	EvaluationTestWithError(String javaExpression) {
		this.javaExpression = javaExpression;
	}

	@Override
	public void executeTest(EvaluationTest test) {
		test.testEvaluationWithError(javaExpression);
	}

	@Override
	public String toString() {
		return javaExpression;
	}
}
