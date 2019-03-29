package dd.kms.zenodot.result;

import dd.kms.zenodot.matching.MatchRating;

import java.util.Collections;
import java.util.Map;

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

	public CompletionSuggestions(int position, Map<CompletionSuggestion, MatchRating> ratedSuggestions) {
		this.position = position;
		this.ratedSuggestions = ratedSuggestions;
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
}
