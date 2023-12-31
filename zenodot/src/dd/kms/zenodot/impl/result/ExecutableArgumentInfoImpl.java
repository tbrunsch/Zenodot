package dd.kms.zenodot.impl.result;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.api.common.GeneralizedExecutable;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import java.util.Map;

public class ExecutableArgumentInfoImpl implements ExecutableArgumentInfo
{
	private final int									currentArgumentIndex;
	private final Map<GeneralizedExecutable, Boolean>	applicableExecutableOverloads;

	public ExecutableArgumentInfoImpl(int currentArgumentIndex, Map<GeneralizedExecutable, Boolean> applicableExecutableOverloads) {
		this.currentArgumentIndex = currentArgumentIndex;
		this.applicableExecutableOverloads = ImmutableMap.copyOf(applicableExecutableOverloads);
	}

	@Override
	public int getCurrentArgumentIndex() {
		return currentArgumentIndex;
	}

	@Override
	public Map<GeneralizedExecutable, Boolean> getApplicableExecutableOverloads() {
		return applicableExecutableOverloads;
	}
}
