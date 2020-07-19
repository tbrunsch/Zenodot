package dd.kms.zenodot.parsers;

import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;

/**
 * Base class of parsers for subexpressions with an object tail and that want the
 * {@link ObjectTailParser} to be called automatically.
 */
abstract class AbstractParserWithObjectTail<C> extends AbstractParser<C, ObjectParseResult, ObjectParseResultExpectation>
{
	AbstractParserWithObjectTail(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	abstract ObjectParseResult parseNext(TokenStream tokenStream, C context, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalErrorException, InternalEvaluationException;

	@Override
	final ObjectParseResult doParse(TokenStream tokenStream, C context, ObjectParseResultExpectation expectation) throws AmbiguousParseResultException, CodeCompletionException, InternalParseException, InternalErrorException, InternalEvaluationException {
		ParseResult nextParseResult = parseNext(tokenStream, context, expectation);
		return ParseResults.parseTail(tokenStream, nextParseResult, parserToolbox, expectation);
	}
}
