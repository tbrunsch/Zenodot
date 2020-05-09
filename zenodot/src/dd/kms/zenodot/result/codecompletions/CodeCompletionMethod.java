package dd.kms.zenodot.result.codecompletions;

import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;

public interface CodeCompletionMethod extends CodeCompletion
{
	ExecutableInfo getMethodInfo();
}
