package dd.kms.zenodot.utils.dataproviders;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.matching.TypeMatch;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.codecompletions.CodeCompletionFactory;
import dd.kms.zenodot.settings.ObjectTreeNode;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

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
		ObjectInfo userObject = node.getUserObject();
		if (userObject.getObject() == null) {
			return TypeMatch.NONE;
		}
		return expectation.rateTypeMatch(userObject.getDeclaredType());
	}

	private MatchRating rateNode(ObjectTreeNode node, String expectedName, ObjectParseResultExpectation expectation) {
		return MatchRatings.create(rateNodeByName(node, expectedName), rateNodeByTypes(node, expectation), false);
	}
}
