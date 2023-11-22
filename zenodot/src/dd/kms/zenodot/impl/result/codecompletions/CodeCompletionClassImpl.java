package dd.kms.zenodot.impl.result.codecompletions;

import dd.kms.zenodot.api.common.ClassInfo;
import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionClass;
import dd.kms.zenodot.framework.result.codecompletions.AbstractSimpleCodeCompletion;
import dd.kms.zenodot.impl.utils.ClassUtils;

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
	public boolean isQualifiedCompletion() {
		return qualifiedCompletion;
	}

	@Override
	public CodeCompletionClass asUnqualifiedCompletion() {
		return isQualifiedCompletion()
			? new CodeCompletionClassImpl(classInfo, getInsertionBegin(), getInsertionEnd(), false, getRating())
			: this;
	}

	@Override
	public String getTextToInsert() {
		return qualifiedCompletion
			? ClassUtils.getRegularClassName(classInfo.getNormalizedName())
			: classInfo.getUnqualifiedName();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(classInfo.getUnqualifiedName());
		String regularClassName = ClassUtils.getRegularClassName(classInfo.getNormalizedName());
		String packageOrParentClassName = ClassUtils.getParentPath(regularClassName);
		if (packageOrParentClassName != null) {
			builder.append(" (").append(packageOrParentClassName).append(")");
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
