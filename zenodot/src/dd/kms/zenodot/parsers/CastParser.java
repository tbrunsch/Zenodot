package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Optional;

/**
 * Parses expressions of the form {@code (<class>) <expression>} in the context of {@code this}.
 */
public class CastParser extends AbstractEntityParser<ObjectInfo>
{
	public CastParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '(') {
			log(LogLevel.ERROR, "expected '('");
			return ParseResults.createParseError(position, "Expected opening parenthesis '('", ErrorPriority.WRONG_PARSER);
		}
		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "potential cast operator; no completion suggestions available");
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		log(LogLevel.INFO, "parsing class at " + tokenStream);
		ParseResult classParseResult = parserToolbox.getClassParser().parse(tokenStream, thisInfo, ParseExpectation.CLASS);
		ParseResultType parseResultType = classParseResult.getResultType();
		log(LogLevel.INFO, "parse result: " + parseResultType);

		Optional<ParseResult> parseResultForPropagation = ParseUtils.prepareParseResultForPropagation(classParseResult, ParseExpectation.CLASS, ErrorPriority.POTENTIALLY_RIGHT_PARSER);
		if (parseResultForPropagation.isPresent()) {
			return parseResultForPropagation.get();
		}
		ClassParseResult parseResult = (ClassParseResult) classParseResult;
		int parsedToPosition = parseResult.getPosition();

		TypeInfo targetType = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != ')') {
			log(LogLevel.ERROR, "missing ')' at " + tokenStream);
			return ParseResults.createParseError(position, "Expected closing parenthesis ')'", ErrorPriority.RIGHT_PARSER);
		}
		log(LogLevel.SUCCESS, "detected cast operator at " + tokenStream);

		if (characterToken.isContainsCaret()) {
			// nothing we can suggest after ')'
			log(LogLevel.INFO, "no completion suggestions available for position " + tokenStream);
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		return parseAndCast(tokenStream, targetType);
	}

	private ParseResult parseAndCast(TokenStream tokenStream, TypeInfo targetType) {
		log(LogLevel.INFO, "parsing object to cast at " + tokenStream);
		ParseResult objectParseResult = parserToolbox.getSimpleExpressionParser().parse(tokenStream, thisInfo, ParseExpectation.OBJECT);

		Optional<ParseResult> parseResultForPropagation = ParseUtils.prepareParseResultForPropagation(objectParseResult, ParseExpectation.OBJECT, ErrorPriority.RIGHT_PARSER);
		if (parseResultForPropagation.isPresent()) {
			return parseResultForPropagation.get();
		}
		ObjectParseResult parseResult = (ObjectParseResult) objectParseResult;
		int parsedToPosition = parseResult.getPosition();
		ObjectInfo objectInfo = parseResult.getObjectInfo();
		tokenStream.moveTo(parsedToPosition);

		try {
			ObjectInfo castInfo = parserToolbox.getObjectInfoProvider().getCastInfo(objectInfo, targetType);
			log(LogLevel.SUCCESS, "successfully casted object");
			return ParseResults.createObjectParseResult(parsedToPosition, castInfo);
		} catch (ClassCastException e) {
			log(LogLevel.ERROR, "class cast exception: " + e.getMessage());
			return ParseResults.createParseError(tokenStream.getPosition(), "Cannot cast expression to '" + targetType + "'", ErrorPriority.RIGHT_PARSER, e);
		}
	}
}
