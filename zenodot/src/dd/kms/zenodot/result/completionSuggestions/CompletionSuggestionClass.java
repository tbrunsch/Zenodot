package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

public interface CompletionSuggestionClass extends CompletionSuggestion
{
	ClassInfo getClassInfo();
}
