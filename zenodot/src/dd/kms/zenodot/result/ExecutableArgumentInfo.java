package dd.kms.zenodot.result;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.utils.wrappers.AbstractExecutableInfo;

import java.util.Map;

public class ExecutableArgumentInfo
{
	private final int									currentArgumentIndex;
	private final Map<AbstractExecutableInfo, Boolean>	applicableExecutableOverloads;

	public ExecutableArgumentInfo(int currentArgumentIndex, Map<AbstractExecutableInfo, Boolean> applicableExecutableOverloads) {
		this.currentArgumentIndex = currentArgumentIndex;
		this.applicableExecutableOverloads = ImmutableMap.copyOf(applicableExecutableOverloads);
	}

	public int getCurrentArgumentIndex() {
		return currentArgumentIndex;
	}

	/**
	 * Returns a map from all executable overloads to Boolean. An executable overload is mapped to true
	 * if and only if it might be applicable for the arguments that have already been parsed.
	 */
	public Map<AbstractExecutableInfo, Boolean> getApplicableExecutableOverloads() {
		return applicableExecutableOverloads;
	}
}
