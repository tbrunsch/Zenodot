package dd.kms.zenodot.utils.dataproviders;

import dd.kms.zenodot.matching.*;
import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.codecompletions.CodeCompletionFactory;
import dd.kms.zenodot.settings.ObjectTreeNode;
import dd.kms.zenodot.utils.ParseUtils;

import java.util.List;

/**
 * Utility class for providing information about {@link ObjectTreeNode}s
 */
public class ObjectTreeNodeDataProvider
{
	public CodeCompletions completeNode(String expectedName, ObjectTreeNode contextNode, int insertionBegin, int insertionEnd) {
		Iterable<? extends ObjectTreeNode> nodes = contextNode.getChildNodes();
		List<CodeCompletion> codeCompletions = ParseUtils.createCodeCompletions(
			nodes,
			node -> CodeCompletionFactory.objectTreeNodeCompletion(node, insertionBegin, insertionEnd, rateNode(node, expectedName))
		);
		return new CodeCompletions(insertionBegin, codeCompletions);
	}

	private StringMatch rateNodeByName(ObjectTreeNode node, String expectedName) {
		return MatchRatings.rateStringMatch(node.getName(), expectedName);
	}

	private MatchRating rateNode(ObjectTreeNode node, String expectedName) {
		return MatchRatings.create(rateNodeByName(node, expectedName), TypeMatch.NONE, AccessMatch.IGNORED);
	}
}
