package dd.kms.zenodot.api.result.codecompletions;

import dd.kms.zenodot.api.result.CodeCompletion;

public interface CodeCompletionClass extends CodeCompletion
{
	Class<?> getClassInfo();
}
