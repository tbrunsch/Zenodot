package dd.kms.zenodot.api.result.codecompletions;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.CodeCompletionType;

public interface CodeCompletionFile extends CodeCompletion
{
	CodeCompletionType FILE	= CodeCompletionType.register("File");
}
