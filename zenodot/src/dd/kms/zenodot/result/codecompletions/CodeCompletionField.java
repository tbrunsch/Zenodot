package dd.kms.zenodot.result.codecompletions;

import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.utils.wrappers.FieldInfo;

public interface CodeCompletionField extends CodeCompletion
{
	FieldInfo getFieldInfo();
}
