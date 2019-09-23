package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestionType;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

import java.util.Objects;

class CompletionSuggestionClassImpl extends AbstractSimpleCompletionSuggestion implements CompletionSuggestionClass
{
	private final ClassInfo classInfo;

	CompletionSuggestionClassImpl(ClassInfo classInfo, int insertionBegin, int insertionEnd) {
		super(CompletionSuggestionType.CLASS, insertionBegin, insertionEnd);
		this.classInfo = classInfo;
	}

	@Override
	public ClassInfo getClassInfo() {
		return classInfo;
	}

	@Override
	public String toString() {
		return classInfo.getUnqualifiedName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CompletionSuggestionClassImpl that = (CompletionSuggestionClassImpl) o;
		return Objects.equals(classInfo, that.classInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), classInfo);
	}
}
