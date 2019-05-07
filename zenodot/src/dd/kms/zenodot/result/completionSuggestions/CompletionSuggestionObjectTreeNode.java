package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.IntRange;
import dd.kms.zenodot.result.IntRanges;
import dd.kms.zenodot.settings.ObjectTreeNode;

import java.util.Objects;

public class CompletionSuggestionObjectTreeNode implements CompletionSuggestion
{
	private final ObjectTreeNode node;
	private final int 				insertionBegin;
	private final int 				insertionEnd;

	public CompletionSuggestionObjectTreeNode(ObjectTreeNode node, int insertionBegin, int insertionEnd) {
		this.node = node;
		this.insertionBegin = insertionBegin;
		this.insertionEnd = insertionEnd;
	}

	@Override
	public IntRange getInsertionRange() {
		return IntRanges.create(insertionBegin, insertionEnd);
	}

	@Override
	public int getCaretPositionAfterInsertion() {
		return insertionBegin + getTextToInsert().length();
	}

	@Override
	public String getTextToInsert() {
		return node.getName();
	}

	@Override
	public String toString() {
		return getTextToInsert();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompletionSuggestionObjectTreeNode that = (CompletionSuggestionObjectTreeNode) o;
		return insertionBegin == that.insertionBegin &&
				insertionEnd == that.insertionEnd &&
				Objects.equals(node, that.node);
	}

	@Override
	public int hashCode() {
		return Objects.hash(node, insertionBegin, insertionEnd);
	}
}
