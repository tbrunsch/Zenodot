package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.result.ParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParseUtils;
import dd.kms.zenodot.framework.utils.ParserToolbox;

/**
 * Base class of parsers for subexpressions with an object tail and that want the
 * {@link ObjectTailParser} to be called automatically.
 */
abstract class AbstractParserWithObjectTail<C> extends AbstractParser<C, ObjectParseResult, ObjectParseResultExpectation>
{
	AbstractParserWithObjectTail(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	abstract ObjectParseResult parseNext(TokenStream tokenStream, C context, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException;

	@Override
	protected final ObjectParseResult doParse(TokenStream tokenStream, C context, ObjectParseResultExpectation expectation) throws CodeCompletionException, SyntaxException, InternalErrorException, EvaluationException {
		ParseResult nextParseResult = parseNext(tokenStream, context, expectation);
		return ParseUtils.parseTail(tokenStream, nextParseResult, parserToolbox, expectation);
	}
}
