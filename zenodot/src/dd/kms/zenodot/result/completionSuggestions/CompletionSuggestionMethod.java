package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;

public interface CompletionSuggestionMethod extends CompletionSuggestion
{
	ExecutableInfo getMethodInfo();
}
