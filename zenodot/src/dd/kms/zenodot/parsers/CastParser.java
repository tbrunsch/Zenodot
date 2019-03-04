package dd.kms.zenodot.parsers;

import dd.kms.zenodot.ParserToolbox;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorType;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

public class CastParser extends AbstractEntityParser<ObjectInfo>
{
	public CastParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '(') {
			log(LogLevel.ERROR, "expected '('");
			return new ParseError(position, "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}
		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "potential cast operator; no completion suggestions available");
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		log(LogLevel.INFO, "parsing class at " + tokenStream);
		ParseResultIF classParseResult = parserToolbox.getClassParser().parse(tokenStream, thisInfo, ParseExpectation.CLASS);
		ParseResultType parseResultType = classParseResult.getResultType();
		log(LogLevel.INFO, "parse result: " + parseResultType);

		if (ParseUtils.propagateParseResult(classParseResult, ParseExpectation.CLASS)) {
			return classParseResult;
		}
		ClassParseResult parseResult = (ClassParseResult) classParseResult;
		int parsedToPosition = parseResult.getPosition();

		TypeInfo targetType = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != ')') {
			log(LogLevel.ERROR, "missing ')' at " + tokenStream);
			return new ParseError(position, "Expected closing parenthesis ')'", ErrorType.SYNTAX_ERROR);
		}
		log(LogLevel.SUCCESS, "detected cast operator at " + tokenStream);

		if (characterToken.isContainsCaret()) {
			// nothing we can suggest after ')'
			log(LogLevel.INFO, "no completion suggestions available for position " + tokenStream);
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		return parseAndCast(tokenStream, targetType);
	}

	private ParseResultIF parseAndCast(TokenStream tokenStream, TypeInfo targetType) {
		log(LogLevel.INFO, "parsing object to cast at " + tokenStream);
		ParseResultIF objectParseResult = parserToolbox.getExpressionParser().parse(tokenStream, thisInfo, ParseExpectation.OBJECT);

		if (ParseUtils.propagateParseResult(objectParseResult, ParseExpectation.OBJECT)) {
			return objectParseResult;
		}
		ObjectParseResult parseResult = (ObjectParseResult) objectParseResult;
		int parsedToPosition = parseResult.getPosition();
		ObjectInfo objectInfo = parseResult.getObjectInfo();
		tokenStream.moveTo(parsedToPosition);

		try {
			ObjectInfo castInfo = parserToolbox.getObjectInfoProvider().getCastInfo(objectInfo, targetType);
			log(LogLevel.SUCCESS, "successfully casted object");
			return new ObjectParseResult(parsedToPosition, castInfo);
		} catch (ClassCastException e) {
			log(LogLevel.ERROR, "class cast exception: " + e.getMessage());
			return new ParseError(tokenStream.getPosition(), "Cannot cast expression to '" + targetType + "'", ErrorType.SEMANTIC_ERROR, e);
		}
	}
}
