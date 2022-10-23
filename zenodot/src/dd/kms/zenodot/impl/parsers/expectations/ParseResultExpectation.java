package dd.kms.zenodot.impl.parsers.expectations;

import dd.kms.zenodot.impl.common.ObjectInfoProvider;
import dd.kms.zenodot.api.result.ParseResult;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.tokenizer.TokenStream;

public interface ParseResultExpectation<T extends ParseResult>
{
	ParseResultExpectation<T> parseWholeText(boolean parseWholeText);

	T check(TokenStream tokenStream, ParseResult parseResult, ObjectInfoProvider objectInfoProvider) throws InternalErrorException, SyntaxException;
}
