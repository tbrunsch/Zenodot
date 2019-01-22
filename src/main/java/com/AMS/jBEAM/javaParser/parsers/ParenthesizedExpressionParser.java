package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.debug.LogLevel;
import com.AMS.jBEAM.javaParser.result.*;
import com.AMS.jBEAM.javaParser.result.ParseError.ErrorType;
import com.AMS.jBEAM.javaParser.tokenizer.Token;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.google.common.reflect.TypeToken;

import java.util.List;

public class ParenthesizedExpressionParser extends AbstractEntityParser
{
	public ParenthesizedExpressionParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<TypeToken<?>> expectedResultTypes) {
		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '(') {
			log(LogLevel.ERROR, "missing '('");
			return new ParseError(position, "Expected opening parenthesis '('", ErrorType.WRONG_PARSER);
		}

		ParseResultIF expressionParseResult = parserContext.getCompoundExpressionParser().parse(tokenStream, currentContextInfo, expectedResultTypes);

		// propagate anything except results
		if (expressionParseResult.getResultType() != ParseResultType.PARSE_RESULT) {
			return expressionParseResult;
		}

		ParseResult parseResult = (ParseResult) expressionParseResult;
		int parsedToPosition = parseResult.getParsedToPosition();
		ObjectInfo objectInfo = parseResult.getObjectInfo();
		tokenStream.moveTo(parsedToPosition);

		characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != ')') {
			log(LogLevel.ERROR, "missing ')'");
			return new ParseError(position, "Expected closing parenthesis ')'", ErrorType.SYNTAX_ERROR);
		}
		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available at " + tokenStream);
			return CompletionSuggestions.NONE;
		}

		return parserContext.getTailParser(false).parse(tokenStream, objectInfo, expectedResultTypes);
	}
}
