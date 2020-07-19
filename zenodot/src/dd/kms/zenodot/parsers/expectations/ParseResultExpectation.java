package dd.kms.zenodot.parsers.expectations;

import dd.kms.zenodot.flowcontrol.InternalErrorException;
import dd.kms.zenodot.flowcontrol.InternalParseException;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.dataproviders.ObjectInfoProvider;

public interface ParseResultExpectation<T extends ParseResult>
{
	ParseResultExpectation<T> parseWholeText(boolean parseWholeText);

	T check(TokenStream tokenStream, ParseResult parseResult, ObjectInfoProvider objectInfoProvider) throws InternalErrorException, InternalParseException;
}
