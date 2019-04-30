package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

/**
 * Base class for {@link ClassTailParser} and {@link ObjectTailParser}
 */
abstract class AbstractTailParser<C> extends AbstractEntityParser<C>
{
	AbstractTailParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	abstract ParseResult parseDot(TokenStream tokenStream, C context, ParseExpectation expectation);
	abstract ParseResult parseOpeningSquareBracket(TokenStream tokenStream, C context, ParseExpectation expectation);
	abstract ParseResult createParseResult(int position, C context);

	@Override
	ParseResult doParse(TokenStream tokenStream, C context, ParseExpectation expectation) {
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
		boolean returnCompletions = tokenStream.isCaretAtPosition() || tokenStream.readOptionalSpace().isContainsCaret();
		return returnCompletions ? CompletionSuggestions.none(position) : createParseResult(position, context);
	}
}
