package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestionType;
import dd.kms.zenodot.utils.ClassUtils;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

import java.util.Objects;

class CompletionSuggestionClassImpl extends AbstractSimpleCompletionSuggestion implements CompletionSuggestionClass
{
	private final ClassInfo classInfo;
	private final boolean	qualifiedSuggestion;

	CompletionSuggestionClassImpl(ClassInfo classInfo, int insertionBegin, int insertionEnd, boolean qualifiedSuggestion) {
		super(CompletionSuggestionType.CLASS, insertionBegin, insertionEnd);
		this.classInfo = classInfo;
		this.qualifiedSuggestion = qualifiedSuggestion;
	}

	@Override
	public ClassInfo getClassInfo() {
		return classInfo;
	}

	@Override
	public String getTextToInsert() {
		return qualifiedSuggestion ? classInfo.getNormalizedName() : classInfo.getUnqualifiedName();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(classInfo.getUnqualifiedName());
		if (qualifiedSuggestion) {
			String packageName = ClassUtils.getParentPath(classInfo.getNormalizedName());
			if (packageName != null) {
				builder.append(" (").append(packageName).append(")");
			}
		}
		return builder.toString();
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
