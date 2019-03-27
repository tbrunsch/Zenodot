package dd.kms.zenodot.evaluationTests.framework;

@FunctionalInterface
public interface TestConfigurator
{
	void configure(EvaluationTest test);
}
