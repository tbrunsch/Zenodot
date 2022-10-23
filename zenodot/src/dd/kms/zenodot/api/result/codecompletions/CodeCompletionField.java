package dd.kms.zenodot.api.result.codecompletions;

import dd.kms.zenodot.api.result.CodeCompletion;

import java.lang.reflect.Field;

public interface CodeCompletionField extends CodeCompletion
{
	Field getField();
}
