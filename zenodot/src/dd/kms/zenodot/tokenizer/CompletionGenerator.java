package dd.kms.zenodot.tokenizer;

import dd.kms.zenodot.flowcontrol.InternalCodeCompletionException;
import dd.kms.zenodot.flowcontrol.InternalParseException;

public interface CompletionGenerator
{
	/**
	 * Create an {@link InternalCodeCompletionException} from {@link CompletionInfo}
	 */
	InternalCodeCompletionException generate(CompletionInfo completionSuggestionInfo) throws InternalParseException;
}
