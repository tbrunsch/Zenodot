package dd.kms.zenodot.api.result.codecompletions;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.settings.ObjectTreeNode;

public interface CodeCompletionObjectTreeNode extends CodeCompletion
{
	CodeCompletionType OBJECT_TREE_NODE	= CodeCompletionType.register("Object Tree Node");

	ObjectTreeNode getObjectTreeNode();
}
