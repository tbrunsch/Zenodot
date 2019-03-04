package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.result.CompletionSuggestionIF;
import dd.kms.zenodot.result.CompletionSuggestionObjectTreeNode;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.settings.ObjectTreeNodeIF;
import dd.kms.zenodot.utils.ParseUtils;

import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public class ObjectTreeNodeDataProvider
{
	public CompletionSuggestions suggestNodes(String expectedName, ObjectTreeNodeIF contextNode, int insertionBegin, int insertionEnd) {
		List<ObjectTreeNodeIF> nodes = contextNode.getChildNodes();
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			nodes,
			node -> new CompletionSuggestionObjectTreeNode(node, insertionBegin, insertionEnd),
			rateNodeByNameFunc(expectedName)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private int rateNodeByName(ObjectTreeNodeIF node, String expectedName) {
		return ParseUtils.rateStringMatch(node.getName(), expectedName);
	}

	private ToIntFunction<ObjectTreeNodeIF> rateNodeByNameFunc(String expectedName) {
		return node -> rateNodeByName(node, expectedName);
	}
}
