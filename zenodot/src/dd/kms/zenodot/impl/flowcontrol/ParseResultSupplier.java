package dd.kms.zenodot.impl.flowcontrol;

import dd.kms.zenodot.api.result.ParseResult;

public interface ParseResultSupplier
{
	ParseResult get() throws SyntaxException, CodeCompletionException;
}
