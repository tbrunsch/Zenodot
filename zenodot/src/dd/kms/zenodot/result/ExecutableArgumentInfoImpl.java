package dd.kms.zenodot.result;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;

import java.util.Map;

class ExecutableArgumentInfoImpl implements ExecutableArgumentInfo
{
	private final int									currentArgumentIndex;
	private final Map<ExecutableInfo, Boolean>	applicableExecutableOverloads;

	ExecutableArgumentInfoImpl(int currentArgumentIndex, Map<ExecutableInfo, Boolean> applicableExecutableOverloads) {
		this.currentArgumentIndex = currentArgumentIndex;
		this.applicableExecutableOverloads = ImmutableMap.copyOf(applicableExecutableOverloads);
	}

	@Override
	public int getCurrentArgumentIndex() {
		return currentArgumentIndex;
	}

	@Override
	public Map<ExecutableInfo, Boolean> getApplicableExecutableOverloads() {
		return applicableExecutableOverloads;
	}
}
