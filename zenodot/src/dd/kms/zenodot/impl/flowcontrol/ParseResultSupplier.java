package dd.kms.zenodot.impl.flowcontrol;

import dd.kms.zenodot.impl.result.ParseResult;

public interface ParseResultSupplier
{
	ParseResult get() throws SyntaxException, CodeCompletionException;
}
