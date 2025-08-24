package dd.kms.zenodotx.tests.evaluation.framework;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class TestData
{
	private final Supplier<Object>	testInstanceProvider;
	@Nullable
	private final TestConfigurator	configureSettings;
	private final TestExecutor		testExecutor;

	TestData(Supplier<Object> testInstanceProvider, @Nullable TestConfigurator configureSettings, TestExecutor testExecutor) {
		this.testInstanceProvider = testInstanceProvider;
		this.configureSettings = configureSettings;
		this.testExecutor = testExecutor;
	}

	Supplier<Object> getTestInstanceProvider() {
		return testInstanceProvider;
	}

	@Nullable
	TestConfigurator getConfigureSettingsFunction() {
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
