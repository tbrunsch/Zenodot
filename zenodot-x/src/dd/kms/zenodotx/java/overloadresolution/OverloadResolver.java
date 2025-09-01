package dd.kms.zenodotx.java.overloadresolution;

import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.framework.matching.MatchRatings;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class OverloadResolver<T>
{
	private final List<Class<?>>	actualParameterTypes;

	private final Map<ParameterDescription, T>	overloadsByParameterDescription	= new LinkedHashMap<>();

	public OverloadResolver(Class<?>... actualParameterTypes) {
		this.actualParameterTypes = Arrays.asList(actualParameterTypes);
	}

	public OverloadResolver<T> registerRegularOverload(T overload, Class<?>... parameterTypes) {
		ParameterDescription parameterDescription = new ParameterDescription(Arrays.asList(parameterTypes), false);
		overloadsByParameterDescription.put(parameterDescription, overload);
		return this;
	}

	public OverloadResolver<T> registerVariadicOverload(T overload, Class<?>... parameterTypes) {
		ParameterDescription parameterDescription = new ParameterDescription(Arrays.asList(parameterTypes), true);
		overloadsByParameterDescription.put(parameterDescription, overload);
		return this;
	}

	/*
	 * Overload resolution happens in at most 3 phases:
	 *   1. Without automatic (un)boxing or variable number of parameters
	 *      (Variadic methods are still considered, but as having an array type)
	 *   2. With automatic (un)boxing, but still without variable number of parameters
	 *   3. With everything
	 *
	 * If in one phase there is at least one applicable overload, then further phases
	 * are not considered.
	 */
	public List<T> getBestMatchingOverloads() {
		Map<ParameterDescription, ParameterMatchInfo> matchInfos = createMatchInfosForBestPhase();
		List<ParameterDescription> bestMatchingParameterDescriptions = getBestMatchingParameterDescriptions(matchInfos);
		return bestMatchingParameterDescriptions.stream().map(overloadsByParameterDescription::get).collect(Collectors.toList());
	}

	private Map<ParameterDescription, ParameterMatchInfo> createMatchInfosForBestPhase() {
		Map<ParameterDescription, ParameterMatchInfo> matchInfos = new LinkedHashMap<>();
		int bestResolutionPhase = Integer.MAX_VALUE;
		for (ParameterDescription parameterDescription : overloadsByParameterDescription.keySet()) {
			ParameterMatchInfo matchInfo = createMatchInfo(parameterDescription);
			if (matchInfo != null) {
				int resolutionPhase = matchInfo.getResolutionPhase();
				if (resolutionPhase < bestResolutionPhase) {
					bestResolutionPhase = resolutionPhase;
					matchInfos.clear();
				}
				if (resolutionPhase == bestResolutionPhase) {
					matchInfos.put(parameterDescription, matchInfo);
				}
			}
		}
		return matchInfos;
	}

	@Nullable
	private ParameterMatchInfo createMatchInfo(ParameterDescription parameterDescription) {
		return parameterDescription.isVariadic()
			? createVariadicMatchInfo(parameterDescription.getParameterTypes())
			: createRegularMatchInfo(parameterDescription.getParameterTypes());
	}

	@Nullable
	private ParameterMatchInfo createVariadicMatchInfo(List<Class<?>> parameterTypes) {
		int numParameters = parameterTypes.size();	// method calls with >= numParameters - 1 are possible
		int numActualParameters = actualParameterTypes.size();
		if (numActualParameters < numParameters - 1) {
			// no match possible
			return null;
		}

		ParameterMatchInfoBuilder builder = new ParameterMatchInfoBuilder();

		// Check non-variadic parameters
		for (int i = 0; i < numParameters - 1; i++) {
			Class<?> actualParameterType = actualParameterTypes.get(i);
			Class<?> parameterType = parameterTypes.get(i);
			if (!builder.addParameter(actualParameterType, parameterType)) {
				// no match
				return null;
			}
		}

		// Check variadic parameter
		Class<?> lastParameterType = parameterTypes.get(numParameters - 1);
		if (!lastParameterType.isArray()) {
			throw new IllegalStateException("Internal error: Last parameter type of a variadic executable must be an array");
		}
		Class<?> componentType = lastParameterType.getComponentType();

		if (numActualParameters == numParameters - 1) {
			builder.setNeedsVariadicFeature();
		} else if (numActualParameters == numParameters) {
			/*
			 * int... matches int[] as well as a single int. However, int[] is matched in Phase 1,
			 * whereas int is matched in Phase 3.
			 */
			Class<?> lastActualParameterType = actualParameterTypes.get(numParameters - 1);
			if (!builder.addParameter(lastActualParameterType, lastParameterType)) {
				builder.setNeedsVariadicFeature();
				if (!builder.addParameter(lastActualParameterType, componentType)) {
					// no match
					return null;
				}
			}
		} else if (numActualParameters > numParameters) {
			builder.setNeedsVariadicFeature();
			for (int i = numParameters - 1; i < numActualParameters; i++) {
				Class<?> actualParameterType = actualParameterTypes.get(i);
				if (!builder.addParameter(actualParameterType, componentType)) {
					// no match
					return null;
				}
			}
		}
		return builder.build();
	}

	@Nullable
	private ParameterMatchInfo createRegularMatchInfo(List<Class<?>> parameterTypes) {
		int numParameters = parameterTypes.size();
		if (actualParameterTypes.size() != numParameters) {
			// no match possible
			return null;
		}

		ParameterMatchInfoBuilder builder = new ParameterMatchInfoBuilder();

		for (int i = 0; i < numParameters; i++) {
			Class<?> actualParameterType = actualParameterTypes.get(i);
			Class<?> parameterType = parameterTypes.get(i);
			if (!builder.addParameter(actualParameterType, parameterType)) {
				// no match
				return null;
			}
		}
		return builder.build();
	}

	private List<ParameterDescription> getBestMatchingParameterDescriptions(Map<ParameterDescription, ParameterMatchInfo> matchInfos) {
		List<ParameterDescription> paretoFront = new ArrayList<>();
		for (ParameterDescription desc : matchInfos.keySet()) {
			ParameterMatchInfo matchInfo = matchInfos.get(desc);
			int numParameters = actualParameterTypes.size();
			if (matchInfo.getParameterTypes().size() != numParameters) {
				throw new IllegalStateException("Internal error: Determined parameter types of a method overload do not match the actual parameter types");
			}

			Iterator<ParameterDescription> iter = paretoFront.iterator();
			boolean dominates = false;
			boolean hasBeenDominated = false;
			while (iter.hasNext()) {
				ParameterDescription paretoDesc = iter.next();
				ParameterMatchInfo paretoMatchInfo = matchInfos.get(paretoDesc);

				int comparisonResult = compareMatchInfos(paretoMatchInfo, matchInfo);
				if (comparisonResult < 0) {
					hasBeenDominated = true;
					/*
					 * We could break here, but we check the remaining elements from the Pareto front
					 * as an additional verification that our comparison implementation is actually
					 * transitive.
					 */
				} else if (comparisonResult > 0) {
					dominates = true;
					iter.remove();
				}
			}
			if (!hasBeenDominated) {
				paretoFront.add(desc);
			} else {
				if (dominates) {
					/*
					 * We have found two elements d1 and d2 in the Pareto front such that d1 < desc < d2.
					 * If we have implemented the comparison correctly, then "<" should be transitive,
					 * which implies d1 < d2. Thus, both elements cannot be in the Pareto front at the
					 * same time.
					 */
					throw new IllegalStateException("Internal error: Current set of matching method overloads contains non-Pareto-optimal solutions");
				}
			}
		}
		return paretoFront;
	}

	private int compareMatchInfos(ParameterMatchInfo matchInfo1, ParameterMatchInfo matchInfo2) {
		return compareParameterTypes(matchInfo1.getParameterTypes(), matchInfo2.getParameterTypes());
	}

	private int compareParameterTypes(List<Class<?>> parameterTypes1, List<Class<?>> parameterTypes2) {
		int numParameters = actualParameterTypes.size();
		if (parameterTypes1.size() != numParameters || parameterTypes2.size() != numParameters) {
			throw new IllegalStateException("Internal error: Both overloads should match, but they have an invalid number of parameters");
		}
		boolean firstOverloadBetterInAParameter = false;
		boolean secondOverloadBetterInAParameter = false;
		for (int i = 0; i < numParameters; i++) {
			Class<?> actualParameterType = actualParameterTypes.get(i);
			Class<?> parameterType1 = parameterTypes1.get(i);
			Class<?> parameterType2 = parameterTypes2.get(i);
			int comparisonResult = compareTypeMatches(parameterType1, parameterType2, actualParameterType);
			if (comparisonResult < 0) {
				firstOverloadBetterInAParameter = true;
			} else if (comparisonResult > 0) {
				secondOverloadBetterInAParameter = true;
			}
		}
		if (firstOverloadBetterInAParameter) {
			return secondOverloadBetterInAParameter ? 0 : -1;
		} else {
			return secondOverloadBetterInAParameter ? 1 : 0;
		}
	}

	private int compareTypeMatches(Class<?> parameterType1, Class<?> parameterType2, Class<?> actualParameterType) {
		if (parameterType1 == parameterType2) {
			return 0;
		}

		TypeMatch typeMatch1 = MatchRatings.rateTypeMatch(parameterType1, actualParameterType);
		TypeMatch typeMatch2 = MatchRatings.rateTypeMatch(parameterType2, actualParameterType);

		int comparisonResult = typeMatch1.compareTo(typeMatch2);
		if (comparisonResult != 0) {
			return comparisonResult;
		}

		switch (typeMatch1) {
			case FULL:
				// this means parameterType1 == actualParameterType == parameterType2 and should have been checked before
				throw new IllegalStateException("Internal error: Both types " + parameterType1 + " and " + parameterType2 + " are claimed to fully match " + actualParameterType);
			case INHERITANCE:
				return compareInheritanceCandidates(parameterType1, parameterType2);
			case WIDENING:
				return compareWideningCandidates(parameterType1, parameterType2);
			case BOXED:
				/*
				 * Possible cases:
				 *   (a) e.g. actualType = int.class, parameterType1 and parameterType2 are Integer.class or a super class
				 *   (b) e.g. actualType = Integer.class, parameterType1 and parameterType2 are int.class or widened classes
				 */
				if (actualParameterType.isPrimitive()) {
					// auto boxing
					if (parameterType1.isPrimitive() || parameterType2.isPrimitive()) {
						throw new IllegalStateException("Internal error: Expected boxed types, but got " + parameterType1 + " and " + parameterType2);
					}
					return compareInheritanceCandidates(parameterType1, parameterType2);
				} else {
					// auto unboxing
					if (!parameterType1.isPrimitive() || !parameterType2.isPrimitive()) {
						throw new IllegalStateException("Internal error: Expected primitive types, but got " + parameterType1 + " and " + parameterType2);
					}
					return compareWideningCandidates(parameterType1, parameterType2);
				}
			default:
				throw new IllegalStateException("Internal error: Unexpected match type " + typeMatch1);
		}
	}

	private int compareInheritanceCandidates(Class<?> parameterType1, Class<?> parameterType2) {
		return	parameterType1.isAssignableFrom(parameterType2)	? 1 :	// parameterType2 is more specific
				parameterType2.isAssignableFrom(parameterType1)	? -1	// parameterType1 is more specific
																: 0;
	}

	private int compareWideningCandidates(Class<?> parameterType1, Class<?> parameterType2) {
		boolean parameter1MoreSpecific = ReflectionUtils.isPrimitiveConvertibleTo(parameterType1, parameterType2, false);
		boolean parameter2MoreSpecific = ReflectionUtils.isPrimitiveConvertibleTo(parameterType2, parameterType1, false);
		if (parameter1MoreSpecific) {
			if (parameter2MoreSpecific) {
				// this implies parameterType1 == parameterType2 and should have been checked before
				throw new IllegalStateException("Internal error: Both types " + parameterType1 + " and " + parameterType2 + " are primitive convertible to each other");
			}
			return -1;
		} else {
			if (!parameter2MoreSpecific) {
				/*
				 * actualParameterType can be widened to parameterType1 and parameterType2,
				 * but parameterType1 and parameterType2 cannot be widened to each other.
				 * This should not be possible.
				 */
				throw new IllegalStateException("Internal error: Type " + parameterType1 + " and " + parameterType2 + "are not convertible to each other.");
			}
			return 1;
		}
	}
}
