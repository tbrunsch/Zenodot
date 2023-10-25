package dd.kms.zenodot.framework.parsers.expectations;

import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.result.ParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;

public interface ParseResultExpectation<T extends ParseResult>
{
	ParseResultExpectation<T> parseWholeText(boolean parseWholeText);

	T check(TokenStream tokenStream, ParseResult parseResult, ObjectInfoProvider objectInfoProvider) throws InternalErrorException, SyntaxException;
}
