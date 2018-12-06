package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.ParserContext;
import com.AMS.jBEAM.javaParser.result.ParseResultIF;
import com.AMS.jBEAM.javaParser.tokenizer.TokenStream;
import com.AMS.jBEAM.javaParser.utils.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;

import java.util.List;

/**
 * Parses an arbitrary Java expression
 */
public class ExpressionParser extends AbstractEntityParser
{
	public ExpressionParser(ParserContext parserContext, ObjectInfo thisInfo) {
		super(parserContext, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo currentContextInfo, List<Class<?>> expectedResultClasses) {
		return ParseUtils.parse(tokenStream, thisInfo, expectedResultClasses,
			parserContext.getLiteralParser(),
			parserContext.getFieldParser(false),
			parserContext.getMethodParser(false),
			parserContext.getParenthesizedExpressionParser(),
			parserContext.getCastParser(),
			parserContext.getClassParser(),
			parserContext.getConstructorParser(),
			parserContext.getUnaryPrefixOperatorParser()
		);
	}
}