package dd.kms.zenodot.impl.utils.dataproviders;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.framework.matching.MatchRatings;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.CodeCompletions;
import dd.kms.zenodot.framework.utils.ParseUtils;
import dd.kms.zenodot.impl.result.codecompletions.CodeCompletionFactory;

import java.util.List;

/**
 * Utility class for providing information about {@link ObjectTreeNode}s
 */
public class ObjectTreeNodeDataProvider
{
	public CodeCompletions completeNode(String expectedName, ObjectTreeNode contextNode, ObjectParseResultExpectation expectation, int insertionBegin, int insertionEnd) {
		Iterable<? extends ObjectTreeNode> nodes = contextNode.getChildNodes();
		List<CodeCompletion> codeCompletions = ParseUtils.createCodeCompletions(
			nodes,
			node -> CodeCompletionFactory.objectTreeNodeCompletion(node, insertionBegin, insertionEnd, rateNode(node, expectedName, expectation))
		);
		return new CodeCompletions(codeCompletions);
	}

	private StringMatch rateNodeByName(ObjectTreeNode node, String expectedName) {
		return MatchRatings.rateStringMatch(expectedName, node.getName());
	}

	private TypeMatch rateNodeByTypes(ObjectTreeNode node, ObjectParseResultExpectation expectation) {
		Object userObject = node.getUserObject();
		if (userObject == null) {
			return TypeMatch.NONE;
		}
		return expectation.rateTypeMatch(userObject.getClass());
	}

	private MatchRating rateNode(ObjectTreeNode node, String expectedName, ObjectParseResultExpectation expectation) {
		return MatchRatings.create(rateNodeByName(node, expectedName), rateNodeByTypes(node, expectation), false);
	}
}
