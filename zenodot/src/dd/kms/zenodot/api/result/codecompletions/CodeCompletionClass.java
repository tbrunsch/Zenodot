package dd.kms.zenodot.api.result.codecompletions;

import dd.kms.zenodot.api.result.CodeCompletion;

public interface CodeCompletionClass extends CodeCompletion
{
	Class<?> getClassInfo();

	/**
	 * Returns whether the code completion returns the fully qualified class name or the
	 * unqualified class name.
	 */
	boolean isQualifiedCompletion();

	/**
	 * Returns the code completion for the same class, but unqualified. If this code completion
	 * is already unqualified, then the same instance is returned.
	 */
	CodeCompletionClass asUnqualifiedCompletion();
}
