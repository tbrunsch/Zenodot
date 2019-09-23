package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.utils.wrappers.FieldInfo;

public interface CompletionSuggestionField extends CompletionSuggestion
{
	FieldInfo getFieldInfo();
}
