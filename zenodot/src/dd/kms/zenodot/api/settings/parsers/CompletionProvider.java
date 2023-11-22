package dd.kms.zenodot.api.settings.parsers;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.CompletionMode;
import dd.kms.zenodot.framework.parsers.CallerContext;
import dd.kms.zenodot.framework.tokenizer.CompletionInfo;

import java.util.List;

@FunctionalInterface
public interface CompletionProvider
{
	List<? extends CodeCompletion> getCodeCompletions(CompletionInfo completionInfo, CompletionMode completionMode, CallerContext callerContext);
}
