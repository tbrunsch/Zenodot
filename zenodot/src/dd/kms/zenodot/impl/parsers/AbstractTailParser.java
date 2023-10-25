package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.framework.result.ParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParserToolbox;

/**
 * Base class for {@link ClassTailParser} and {@link ObjectTailParser}
 */
abstract class AbstractTailParser<C, T extends ParseResult, S extends ParseResultExpectation<T>> extends AbstractParser<C, T, S>
{
	AbstractTailParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	abstract ParseResult parseDot(TokenStream tokenStream, C context, S expectation) throws CodeCompletionException, SyntaxException, EvaluationException, InternalErrorException;
	abstract ParseResult parseOpeningSquareBracket(TokenStream tokenStream, C context, S expectation) throws SyntaxException, CodeCompletionException, EvaluationException, InternalErrorException;
	abstract ParseResult createParseResult(TokenStream tokenStream, C context);

	@Override
	protected ParseResult doParse(TokenStream tokenStream, C context, S expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		char tailCharacter = tokenStream.peekCharacter();
		if (tailCharacter == '.') {
			tokenStream.readCharacter('.');
			log(LogLevel.INFO, "detected '.'");
			increaseConfidence(ParserConfidence.RIGHT_PARSER);
			return parseDot(tokenStream, context, expectation);
		} else if (tailCharacter == '[') {
			log(LogLevel.INFO, "detected '['");
			increaseConfidence(ParserConfidence.RIGHT_PARSER);
			return parseOpeningSquareBracket(tokenStream, context, expectation);
		} else {
			return createParseResult(tokenStream, context);
		}
	}
}
