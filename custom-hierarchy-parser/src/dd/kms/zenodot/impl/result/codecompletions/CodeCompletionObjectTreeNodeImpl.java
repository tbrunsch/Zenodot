package dd.kms.zenodot.impl.result.codecompletions;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionObjectTreeNode;
import dd.kms.zenodot.api.settings.ObjectTreeNode;

import java.util.Objects;

public class CodeCompletionObjectTreeNodeImpl extends AbstractSimpleCodeCompletion implements CodeCompletionObjectTreeNode
{
	private final ObjectTreeNode	node;

	public CodeCompletionObjectTreeNodeImpl(ObjectTreeNode node, int insertionBegin, int insertionEnd, MatchRating rating) {
		super(CodeCompletionObjectTreeNode.OBJECT_TREE_NODE, insertionBegin, insertionEnd, rating);
		this.node = node;
	}

	@Override
	public ObjectTreeNode getObjectTreeNode() {
		return node;
	}

	@Override
	public String toString() {
		return node.getName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CodeCompletionObjectTreeNodeImpl that = (CodeCompletionObjectTreeNodeImpl) o;
		return Objects.equals(node, that.node);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), node);
	}
}
