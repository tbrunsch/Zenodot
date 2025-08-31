package dd.kms.zenodotx.java.overloadresolution;

import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.framework.matching.MatchRatings;

import java.util.ArrayList;
import java.util.List;

class ParameterMatchInfoBuilder
{
	private final List<Class<?>>	parameterTypes	= new ArrayList<>();
	private int						resolutionPhase	= 1;

	boolean addParameter(Class<?> actualParameterType, Class<?> parameterType) {
		int firstPhaseToMatch = getFirstPhaseToMatch(actualParameterType, parameterType);
		if (firstPhaseToMatch < 0) {
			return false;
		}
		resolutionPhase = Math.max(resolutionPhase, firstPhaseToMatch);
		parameterTypes.add(parameterType);
		return true;
	}

	void setNeedsVariadicFeature() {
		resolutionPhase = 3;
	}

	ParameterMatchInfo build() {
		return new ParameterMatchInfo(parameterTypes, resolutionPhase);
	}

	private int getFirstPhaseToMatch(Class<?> actualParameterType, Class<?> parameterType) {
		TypeMatch typeMatch = MatchRatings.rateTypeMatch(parameterType, actualParameterType);
		switch (typeMatch) {
			case FULL:
			case INHERITANCE:
			case WIDENING:
				return 1;
			case BOXED:
				return 2;
			default:
				return -1;
		}
	}
}
