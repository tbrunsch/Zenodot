package dd.kms.zenodot.tokenizer;

import dd.kms.zenodot.flowcontrol.SyntaxException;
import dd.kms.zenodot.result.CodeCompletions;

public interface CompletionGenerator
{
	/**
	 * Create {@link CodeCompletions} from {@link CompletionInfo}
	 */
	CodeCompletions generate(CompletionInfo completionInfo) throws SyntaxException;
}
