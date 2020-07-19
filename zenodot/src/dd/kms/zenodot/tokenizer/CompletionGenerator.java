package dd.kms.zenodot.tokenizer;

import dd.kms.zenodot.flowcontrol.InternalParseException;
import dd.kms.zenodot.result.CodeCompletions;

public interface CompletionGenerator
{
	/**
	 * Create {@link CodeCompletions} from {@link CompletionInfo}
	 */
	CodeCompletions generate(CompletionInfo completionInfo) throws InternalParseException;
}
