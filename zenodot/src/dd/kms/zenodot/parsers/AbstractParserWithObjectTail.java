package dd.kms.zenodot.parsers;

import dd.kms.zenodot.result.ParseOutcome;
import dd.kms.zenodot.result.ParseOutcomes;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;

abstract class AbstractParserWithObjectTail<C> extends AbstractParser<C>
{
	AbstractParserWithObjectTail(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	abstract ParseOutcome parseNext(TokenStream tokenStream, C context, ParseExpectation expectation);

	@Override
	final ParseOutcome doParse(TokenStream tokenStream, C context, ParseExpectation expectation) {
		ParseOutcome nextParseOutcome = parseNext(tokenStream, context, expectation);
		return ParseOutcomes.parseTail(tokenStream, nextParseOutcome, parserToolbox, expectation);
	}
}
