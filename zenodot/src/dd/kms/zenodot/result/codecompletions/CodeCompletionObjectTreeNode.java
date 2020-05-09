package dd.kms.zenodot.result.codecompletions;

import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.settings.ObjectTreeNode;

public interface CodeCompletionObjectTreeNode extends CodeCompletion
{
	ObjectTreeNode getObjectTreeNode();
}
