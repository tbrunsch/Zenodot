package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestionType;
import dd.kms.zenodot.settings.ObjectTreeNode;

import java.util.Objects;

class CompletionSuggestionObjectTreeNodeImpl extends AbstractSimpleCompletionSuggestion
{
	private final ObjectTreeNode	node;

	CompletionSuggestionObjectTreeNodeImpl(ObjectTreeNode node, int insertionBegin, int insertionEnd) {
		super(CompletionSuggestionType.OBJECT_TREE_NODE, insertionBegin, insertionEnd);
		this.node = node;
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
		CompletionSuggestionObjectTreeNodeImpl that = (CompletionSuggestionObjectTreeNodeImpl) o;
		return Objects.equals(node, that.node);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), node);
	}
}
