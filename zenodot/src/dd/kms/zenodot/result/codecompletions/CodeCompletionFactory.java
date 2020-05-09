package dd.kms.zenodot.result.codecompletions;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.settings.ObjectTreeNode;
import dd.kms.zenodot.settings.Variable;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.FieldInfo;

public class CodeCompletionFactory
{
	public static CodeCompletion classCompletion(ClassInfo classInfo, int insertionBegin, int insertionEnd, boolean qualifiedCompletion, MatchRating rating) {
		return new CodeCompletionClassImpl(classInfo, insertionBegin, insertionEnd, qualifiedCompletion, rating);
	}

	public static CodeCompletion fieldCompletion(FieldInfo fieldInfo, int insertionBegin, int insertionEnd, MatchRating rating) {
		return new CodeCompletionFieldImpl(fieldInfo, insertionBegin, insertionEnd, rating);
	}

	public static CodeCompletion keywordCompletion(String keyword, int insertionBegin, int insertionEnd, MatchRating rating) {
		return new CodeCompletionKeywordImpl(keyword, insertionBegin, insertionEnd, rating);
	}

	public static CodeCompletion methodCompletion(ExecutableInfo methodInfo, int insertionBegin, int insertionEnd, MatchRating rating) {
		return new CodeCompletionMethodImpl(methodInfo, insertionBegin, insertionEnd, rating);
	}

	public static CodeCompletion objectTreeNodeCompletion(ObjectTreeNode node, int insertionBegin, int insertionEnd, MatchRating rating) {
		return new CodeCompletionObjectTreeNodeImpl(node, insertionBegin, insertionEnd, rating);
	}

	public static CodeCompletion packageCompletion(String packageName, int insertionBegin, int insertionEnd, MatchRating rating) {
		return new CodeCompletionPackageImpl(packageName, insertionBegin, insertionEnd, rating);
	}

	public static CodeCompletion variableCompletion(Variable variable, int insertionBegin, int insertionEnd, MatchRating rating) {
		return new CodeCompletionVariableImpl(variable, insertionBegin, insertionEnd, rating);
	}
}
