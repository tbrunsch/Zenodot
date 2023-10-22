package dd.kms.zenodot.impl.result.codecompletions;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.ObjectTreeNode;

public class ObjectTreeNodeCodeCompletionFactory
{
	public static CodeCompletion objectTreeNodeCompletion(ObjectTreeNode node, int insertionBegin, int insertionEnd, MatchRating rating) {
		return new CodeCompletionObjectTreeNodeImpl(node, insertionBegin, insertionEnd, rating);
	}
}
