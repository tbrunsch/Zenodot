package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.settings.ObjectTreeNode;
import dd.kms.zenodot.settings.Variable;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.FieldInfo;

public class CompletionSuggestionFactory
{
	public static CompletionSuggestion classSuggestions(ClassInfo classInfo, int insertionBegin, int insertionEnd) {
		return new CompletionSuggestionClassImpl(classInfo, insertionBegin, insertionEnd);
	}

	public static CompletionSuggestion fieldSuggestion(FieldInfo fieldInfo, int insertionBegin, int insertionEnd) {
		return new CompletionSuggestionFieldImpl(fieldInfo, insertionBegin, insertionEnd);
	}

	public static CompletionSuggestion keywordSuggestion(String keyword, int insertionBegin, int insertionEnd) {
		return new CompletionSuggestionKeywordImpl(keyword, insertionBegin, insertionEnd);
	}

	public static CompletionSuggestion methodSuggestion(ExecutableInfo methodInfo, int insertionBegin, int insertionEnd) {
		return new CompletionSuggestionMethodImpl(methodInfo, insertionBegin, insertionEnd);
	}

	public static CompletionSuggestion objectTreeNodeSuggestion(ObjectTreeNode node, int insertionBegin, int insertionEnd) {
		return new CompletionSuggestionObjectTreeNodeImpl(node, insertionBegin, insertionEnd);
	}

	public static CompletionSuggestion packageSuggestion(String packageName, int insertionBegin, int insertionEnd) {
		return new CompletionSuggestionPackageImpl(packageName, insertionBegin, insertionEnd);
	}

	public static CompletionSuggestion variableSuggestion(Variable variable, int insertionBegin, int insertionEnd) {
		return new CompletionSuggestionVariableImpl(variable, insertionBegin, insertionEnd);
	}
}
