package dd.kms.zenodot.impl.tokenizer;

import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.result.CodeCompletions;

public interface CompletionGenerator
{
	/**
	 * Create {@link CodeCompletions} from {@link CompletionInfo}
	 */
	CodeCompletions generate(CompletionInfo completionInfo) throws SyntaxException;
}
