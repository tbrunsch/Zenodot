package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestionType;

import java.util.Objects;

class CompletionSuggestionPackageImpl extends AbstractSimpleCompletionSuggestion implements CompletionSuggestionPackage
{
	private final String	packageName;

	CompletionSuggestionPackageImpl(String packageName, int insertionBegin, int insertionEnd) {
		super(CompletionSuggestionType.PACKAGE, insertionBegin, insertionEnd);
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
		CompletionSuggestionPackageImpl that = (CompletionSuggestionPackageImpl) o;
		return Objects.equals(packageName, that.packageName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), packageName);
	}
}
