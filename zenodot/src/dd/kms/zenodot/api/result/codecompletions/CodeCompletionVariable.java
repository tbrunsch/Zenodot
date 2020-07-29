package dd.kms.zenodot.api.result.codecompletions;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.Variable;

public interface CodeCompletionVariable extends CodeCompletion
{
	Variable getVariable();
}
