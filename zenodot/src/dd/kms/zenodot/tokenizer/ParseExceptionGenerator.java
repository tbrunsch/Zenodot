package dd.kms.zenodot.tokenizer;

import dd.kms.zenodot.flowcontrol.InternalParseException;

public interface ParseExceptionGenerator
{
	/**
	 * Create an {@link InternalParseException} for {@code tokenStream} at position {@link TokenStream#getPosition()}.
	 */
	InternalParseException generate(TokenStream tokenStream);
}
