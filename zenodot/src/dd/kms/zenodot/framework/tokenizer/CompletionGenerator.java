package dd.kms.zenodot.framework.tokenizer;

import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.result.CodeCompletions;

public interface CompletionGenerator
{
	/**
	 * Create {@link CodeCompletions} from {@link CompletionInfo}
	 */
	CodeCompletions generate(CompletionInfo completionInfo) throws SyntaxException;
}
