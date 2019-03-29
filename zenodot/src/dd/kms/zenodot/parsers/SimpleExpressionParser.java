package dd.kms.zenodot.parsers;

import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

/**
 * Parses an arbitrary Java expression without binary operators. Use the {@link ExpressionParser}
 * if binary operators should be considered as well.
 */
public class SimpleExpressionParser extends AbstractEntityParser<ObjectInfo>
{
	public SimpleExpressionParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		return ParseUtils.parse(tokenStream, contextInfo, expectation,
			parserToolbox.getLiteralParser(),
			parserToolbox.getObjectFieldParser(),
			parserToolbox.getObjectMethodParser(),
			parserToolbox.getParenthesizedExpressionParser(),
			parserToolbox.getCastParser(),
			parserToolbox.getClassParser(),
			parserToolbox.getConstructorParser(),
			parserToolbox.getUnaryPrefixOperatorParser(),
			parserToolbox.getVariableParser(),
			parserToolbox.getCustomHierarchyParser()
		);
	}
}
