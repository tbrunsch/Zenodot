package dd.kms.zenodot.impl.result;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import java.lang.reflect.Executable;
import java.util.Map;

class ExecutableArgumentInfoImpl implements ExecutableArgumentInfo
{
	private final int						currentArgumentIndex;
	private final Map<Executable, Boolean>	applicableExecutableOverloads;

	ExecutableArgumentInfoImpl(int currentArgumentIndex, Map<Executable, Boolean> applicableExecutableOverloads) {
		this.currentArgumentIndex = currentArgumentIndex;
		this.applicableExecutableOverloads = ImmutableMap.copyOf(applicableExecutableOverloads);
	}

	@Override
	public int getCurrentArgumentIndex() {
		return currentArgumentIndex;
	}

	@Override
	public Map<Executable, Boolean> getApplicableExecutableOverloads() {
		return applicableExecutableOverloads;
	}
}
