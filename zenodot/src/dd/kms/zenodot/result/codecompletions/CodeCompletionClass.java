package dd.kms.zenodot.result.codecompletions;

import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

public interface CodeCompletionClass extends CodeCompletion
{
	ClassInfo getClassInfo();
}
