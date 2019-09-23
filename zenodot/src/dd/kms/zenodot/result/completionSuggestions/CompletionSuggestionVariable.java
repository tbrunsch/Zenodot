package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.settings.Variable;

public interface CompletionSuggestionVariable extends CompletionSuggestion
{
	Variable getVariable();
}
