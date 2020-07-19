package dd.kms.zenodot.flowcontrol;

import dd.kms.zenodot.result.ParseResult;

public interface ParseResultSupplier
{
	ParseResult get() throws AmbiguousParseResultException, InternalParseException, CodeCompletionException;
}
