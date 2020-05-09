package dd.kms.zenodot.result.codecompletions;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.result.CodeCompletionType;
import dd.kms.zenodot.utils.ClassUtils;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

import java.util.Objects;

class CodeCompletionClassImpl extends AbstractSimpleCodeCompletion implements CodeCompletionClass
{
	private final ClassInfo classInfo;
	private final boolean	qualifiedCompletion;

	CodeCompletionClassImpl(ClassInfo classInfo, int insertionBegin, int insertionEnd, boolean qualifiedCompletion, MatchRating rating) {
		super(CodeCompletionType.CLASS, insertionBegin, insertionEnd, rating);
		this.classInfo = classInfo;
		this.qualifiedCompletion = qualifiedCompletion;
	}

	@Override
	public ClassInfo getClassInfo() {
		return classInfo;
	}

	@Override
	public String getTextToInsert() {
		return qualifiedCompletion ? classInfo.getNormalizedName() : classInfo.getUnqualifiedName();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(classInfo.getUnqualifiedName());
		if (qualifiedCompletion) {
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
		CodeCompletionClassImpl that = (CodeCompletionClassImpl) o;
		return Objects.equals(classInfo, that.classInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), classInfo);
	}
}
