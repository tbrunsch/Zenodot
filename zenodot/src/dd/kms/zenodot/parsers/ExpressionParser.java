package dd.kms.zenodot.parsers;

import dd.kms.zenodot.ParserToolbox;
import dd.kms.zenodot.result.ParseResultIF;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

/**
 * Parses an arbitrary Java expression without binary operators
 */
public class ExpressionParser extends AbstractEntityParser<ObjectInfo>
{
	public ExpressionParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
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
