package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.ParseOutcome;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

/**
 * Base class for {@link ClassTailParser} and {@link ObjectTailParser}
 */
abstract class AbstractTailParser<C> extends AbstractParser<C>
{
	AbstractTailParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	abstract ParseOutcome parseDot(TokenStream tokenStream, C context, ParseExpectation expectation);
	abstract ParseOutcome parseOpeningSquareBracket(TokenStream tokenStream, C context, ParseExpectation expectation);
	abstract ParseOutcome createParseOutcome(int position, C context);

	@Override
	ParseOutcome doParse(TokenStream tokenStream, C context, ParseExpectation expectation) {
		if (tokenStream.hasMore()) {
			char nextChar = tokenStream.peekCharacter();
			if (nextChar == '.') {
				log(LogLevel.INFO, "detected '.'");
				return parseDot(tokenStream, context, expectation);
			} else if (nextChar == '[') {
				log(LogLevel.INFO, "detected '['");
				return parseOpeningSquareBracket(tokenStream, context, expectation);
			}
		}

		int position = tokenStream.getPosition();
		boolean returnCompletions = tokenStream.isCaretWithinNextWhiteSpaces();
		return returnCompletions ? CompletionSuggestions.none(position) : createParseOutcome(position, context);
	}
}
