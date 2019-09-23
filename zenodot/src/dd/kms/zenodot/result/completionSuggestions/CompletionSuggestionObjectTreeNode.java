package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.settings.ObjectTreeNode;

public interface CompletionSuggestionObjectTreeNode extends CompletionSuggestion
{
	ObjectTreeNode getObjectTreeNode();
}
