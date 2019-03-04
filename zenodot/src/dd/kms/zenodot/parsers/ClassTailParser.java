package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.result.ClassParseResult;
import dd.kms.zenodot.result.ParseResultIF;
import dd.kms.zenodot.result.ParseResultType;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * Parses a sub expression following a complete Java expression, assuming the context {@code <class>
 */
public class ClassTailParser extends AbstractTailParser<TypeInfo>
{
	public ClassTailParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResultIF parseDot(TokenStream tokenStream, TypeInfo classType, ParseExpectation expectation) {
		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals(".");

		AbstractEntityParser<TypeInfo> fieldParser = parserToolbox.getClassFieldParser();
		AbstractEntityParser<TypeInfo> methodParser = parserToolbox.getClassMethodParser();
		AbstractEntityParser<TypeInfo> innerClassParser = parserToolbox.getInnerClassParser();
		if (expectation.getEvaluationType() == ParseResultType.CLASS_PARSE_RESULT) {
			return innerClassParser.parse(tokenStream, classType, expectation);
		} else {
			return ParseUtils.parse(tokenStream, classType, expectation,
				fieldParser,
				methodParser,
				innerClassParser
			);
		}
	}

	@Override
	ParseResultIF parseOpeningSquareBracket(TokenStream tokenStream, TypeInfo context, ParseExpectation expectation) {
		/*
		 * If called under ConstructorParser, then this is an array construction. As we do not
		 * know, in which circumstances this method is called, the caller must handle this
		 * operator. Hence, we stop parsing here.
		 */
		return new ClassParseResult(tokenStream.getPosition(), context);
	}

	@Override
	ParseResultIF createParseResult(int position, TypeInfo type) {
		return new ClassParseResult(position, type);
	}
}
