package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.matching.*;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionObjectTreeNode;
import dd.kms.zenodot.settings.ObjectTreeNode;
import dd.kms.zenodot.utils.ParseUtils;

import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for providing information about {@link ObjectTreeNode}s
 */
public class ObjectTreeNodeDataProvider
{
	public CompletionSuggestions suggestNodes(String expectedName, ObjectTreeNode contextNode, int insertionBegin, int insertionEnd) {
		Iterable<ObjectTreeNode> nodes = contextNode.getChildNodes();
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
			nodes,
			node -> new CompletionSuggestionObjectTreeNode(node, insertionBegin, insertionEnd),
			rateNodeFunc(expectedName)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private StringMatch rateNodeByName(ObjectTreeNode node, String expectedName) {
		return MatchRatings.rateStringMatch(node.getName(), expectedName);
	}

	private Function<ObjectTreeNode, MatchRating> rateNodeFunc(String expectedName) {
		return node -> new MatchRating(rateNodeByName(node, expectedName), TypeMatch.NONE, AccessMatch.IGNORED);
	}
}
