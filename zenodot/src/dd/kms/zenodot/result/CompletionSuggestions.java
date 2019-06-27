package dd.kms.zenodot.result;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.matching.MatchRating;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * A collection of {@link CompletionSuggestion} including their ratings
 */
public class CompletionSuggestions implements ParseOutcome
{
	public static CompletionSuggestions none(int position) {
		return new CompletionSuggestions(position, Collections.emptyMap());
	}

	public static CompletionSuggestions of(CompletionSuggestion suggestion, MatchRating rating) {
		ImmutableMap.Builder<CompletionSuggestion, MatchRating> ratingBuilder = ImmutableMap.builder();
		ratingBuilder.put(suggestion, rating);
		return new CompletionSuggestions(suggestion.getInsertionRange().getEnd(), ratingBuilder.build());
	}

	private final int										position;
	private final Map<CompletionSuggestion, MatchRating>	ratedSuggestions;
	private final Optional<ExecutableArgumentInfo>			executableArgumentInfo;

	public CompletionSuggestions(int position, Map<CompletionSuggestion, MatchRating> ratedSuggestions) {
		this(position, ratedSuggestions, Optional.empty());
	}

	public CompletionSuggestions(int position, Map<CompletionSuggestion, MatchRating> ratedSuggestions, Optional<ExecutableArgumentInfo> executableArgumentInfo) {
		this.position = position;
		this.ratedSuggestions = ratedSuggestions;
		this.executableArgumentInfo = executableArgumentInfo;
	}

	@Override
	public ParseOutcomeType getOutcomeType() {
		return ParseOutcomeType.COMPLETION_SUGGESTIONS;
	}

	@Override
	public int getPosition() {
		return position;
	}

	public Map<CompletionSuggestion, MatchRating> getRatedSuggestions() {
		return ratedSuggestions;
	}

	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo() {
		return executableArgumentInfo;
	}
}
