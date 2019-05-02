package dd.kms.zenodot.result;

import dd.kms.zenodot.matching.MatchRating;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * A collection of {@link CompletionSuggestion} including their ratings
 */
public class CompletionSuggestions implements ParseResult
{
	public static final CompletionSuggestions none(int position) {
		return new CompletionSuggestions(position, Collections.emptyMap());
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
	public ParseResultType getResultType() {
		return ParseResultType.COMPLETION_SUGGESTIONS;
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
