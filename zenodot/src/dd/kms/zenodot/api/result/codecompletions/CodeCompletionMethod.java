package dd.kms.zenodot.api.result.codecompletions;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.wrappers.ExecutableInfo;

public interface CodeCompletionMethod extends CodeCompletion
{
	ExecutableInfo getMethodInfo();
}
