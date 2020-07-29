package dd.kms.zenodot.api.result.codecompletions;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.wrappers.FieldInfo;

public interface CodeCompletionField extends CodeCompletion
{
	FieldInfo getFieldInfo();
}
