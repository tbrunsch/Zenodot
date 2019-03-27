package dd.kms.zenodot.completionTests.framework;

@FunctionalInterface
public interface TestConfigurator
{
	void configure(CompletionTest test);
}
