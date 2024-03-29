package dd.kms.zenodot.impl.result.codecompletions;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionPackage;
import dd.kms.zenodot.framework.result.codecompletions.AbstractSimpleCodeCompletion;

import java.util.Objects;

class CodeCompletionPackageImpl extends AbstractSimpleCodeCompletion implements CodeCompletionPackage
{
	private final String	packageName;

	CodeCompletionPackageImpl(String packageName, int insertionBegin, int insertionEnd, MatchRating rating) {
		super(CodeCompletionType.PACKAGE, insertionBegin, insertionEnd, rating);
		this.packageName = packageName;
	}

	@Override
	public String getPackageName() {
		return packageName;
	}

	@Override
	public String toString() {
		int lastDotIndex = packageName.lastIndexOf('.');
		return lastDotIndex < 0 ? packageName : packageName.substring(lastDotIndex + 1);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CodeCompletionPackageImpl that = (CodeCompletionPackageImpl) o;
		return Objects.equals(packageName, that.packageName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), packageName);
	}
}
