package dd.kms.zenodot.api.result.codecompletions;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.wrappers.ClassInfo;

public interface CodeCompletionClass extends CodeCompletion
{
	ClassInfo getClassInfo();
}
