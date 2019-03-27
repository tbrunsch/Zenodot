package dd.kms.zenodot.completionTests.framework;

import javax.annotation.Nullable;

public class TestData
{
	private final Object						testInstance;
	private final @Nullable TestConfigurator	configureSettings;
	private final TestExecutor					testExecutor;

	TestData(Object testInstance, @Nullable TestConfigurator configureSettings, TestExecutor testExecutor) {
		this.testInstance = testInstance;
		this.configureSettings = configureSettings;
		this.testExecutor = testExecutor;
	}

	Object getTestInstance() {
		return testInstance;
	}

	@Nullable TestConfigurator getConfigureSettingsFunction() {
		return configureSettings;
	}

	TestExecutor getTestExecutor() {
		return testExecutor;
	}

	@Override
	public String toString() {
		return testExecutor.toString();
	}
}
