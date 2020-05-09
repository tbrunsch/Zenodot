package dd.kms.zenodot.result.codecompletions;

import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.settings.Variable;

public interface CodeCompletionVariable extends CodeCompletion
{
	Variable getVariable();
}
