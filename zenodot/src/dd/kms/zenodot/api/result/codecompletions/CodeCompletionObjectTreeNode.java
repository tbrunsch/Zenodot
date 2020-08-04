package dd.kms.zenodot.api.result.codecompletions;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.ObjectTreeNode;

public interface CodeCompletionObjectTreeNode extends CodeCompletion
{
	ObjectTreeNode getObjectTreeNode();
}
