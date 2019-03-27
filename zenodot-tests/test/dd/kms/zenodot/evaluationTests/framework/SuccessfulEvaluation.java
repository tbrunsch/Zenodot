package dd.kms.zenodot.evaluationTests.framework;

class SuccessfulEvaluation implements TestExecutor
{
	private final String	javaExpression;
	private final Object	expectedValue;

	SuccessfulEvaluation(String javaExpression, Object expectedValue) {
		this.javaExpression = javaExpression;
		this.expectedValue = expectedValue;
	}

	@Override
	public void executeTest(EvaluationTest test) {
		test.testEvaluation(javaExpression, expectedValue);
	}

	@Override
	public String toString() {
		return javaExpression;
	}
}
