package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.matching.TypeMatch;
import dd.kms.zenodot.result.CompletionSuggestionIF;
import dd.kms.zenodot.result.CompletionSuggestionObjectTreeNode;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.settings.ObjectTreeNodeIF;
import dd.kms.zenodot.utils.ParseUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ObjectTreeNodeDataProvider
{
	public CompletionSuggestions suggestNodes(String expectedName, ObjectTreeNodeIF contextNode, int insertionBegin, int insertionEnd) {
		List<ObjectTreeNodeIF> nodes = contextNode.getChildNodes();
		Map<CompletionSuggestionIF, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
			nodes,
			node -> new CompletionSuggestionObjectTreeNode(node, insertionBegin, insertionEnd),
			rateNodeFunc(expectedName)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private StringMatch rateNodeByName(ObjectTreeNodeIF node, String expectedName) {
		return MatchRatings.rateStringMatch(node.getName(), expectedName);
	}

	private Function<ObjectTreeNodeIF, MatchRating> rateNodeFunc(String expectedName) {
		return node -> new MatchRating(rateNodeByName(node, expectedName), TypeMatch.NONE);
	}
}