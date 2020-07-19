package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;

/**
 * Base class for {@link ClassTailParser} and {@link ObjectTailParser}
 */
abstract class AbstractTailParser<C, T extends ParseResult, S extends ParseResultExpectation<T>> extends AbstractParser<C, T, S>
{
	AbstractTailParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	abstract ParseResult parseDot(TokenStream tokenStream, C context, S expectation) throws AmbiguousParseResultException, CodeCompletionException, InternalParseException, InternalEvaluationException, InternalErrorException;
	abstract ParseResult parseOpeningSquareBracket(TokenStream tokenStream, C context, S expectation) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalEvaluationException, InternalErrorException;
	abstract ParseResult createParseResult(C context);

	@Override
	ParseResult doParse(TokenStream tokenStream, C context, S expectation) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalErrorException, InternalEvaluationException {
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
			return createParseResult(context);
		}
	}
}
