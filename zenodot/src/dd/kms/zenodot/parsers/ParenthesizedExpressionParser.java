package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.result.ParseError;
import dd.kms.zenodot.result.ParseError.ErrorType;
import dd.kms.zenodot.result.ParseResultIF;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

/**
 * Parses expressions of the form {@code (<expression>)} in the context of {@code this}.
 */
public class ParenthesizedExpressionParser extends AbstractEntityParser<ObjectInfo>
{
	public ParenthesizedExpressionParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '(') {
			log(LogLevel.ERROR, "missing '('");
			return new ParseError(position, "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}

		ParseResultIF expressionParseResult = parserToolbox.getExpressionParser().parse(tokenStream, contextInfo, expectation);

		if (ParseUtils.propagateParseResult(expressionParseResult, expectation)) {
			return expressionParseResult;
		}

		ObjectParseResult parseResult = (ObjectParseResult) expressionParseResult;
		int parsedToPosition = parseResult.getPosition();
		ObjectInfo objectInfo = parseResult.getObjectInfo();
		tokenStream.moveTo(parsedToPosition);

		characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != ')') {
			log(LogLevel.ERROR, "missing ')'");
			return new ParseError(position, "Expected closing parenthesis ')'", ErrorType.SYNTAX_ERROR);
		}
		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available at " + tokenStream);
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		return parserToolbox.getObjectTailParser().parse(tokenStream, objectInfo, expectation);
	}
}
