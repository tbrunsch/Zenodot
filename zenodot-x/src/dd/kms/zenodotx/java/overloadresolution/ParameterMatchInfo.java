package dd.kms.zenodotx.java.overloadresolution;

import java.util.List;

class ParameterMatchInfo
{
	private final List<Class<?>>	parameterTypes;
	private final int				resolutionPhase;

	ParameterMatchInfo(List<Class<?>> parameterTypes, int resolutionPhase) {
		this.parameterTypes = parameterTypes;
		this.resolutionPhase = resolutionPhase;
	}

	List<Class<?>> getParameterTypes() {
		return parameterTypes;
	}

	int getResolutionPhase() {
		return resolutionPhase;
	}
}
