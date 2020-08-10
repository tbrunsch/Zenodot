package dd.kms.zenodot.tests.completionTests.framework;

@FunctionalInterface
public interface TestConfigurator
{
	void configure(CompletionTest test);
}
