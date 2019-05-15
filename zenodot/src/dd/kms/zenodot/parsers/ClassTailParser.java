package dd.kms.zenodot.parsers;

import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.result.ParseResultType;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * Parses subexpressions
 * <ul>
 *     <li>{@code .<static field>} of expressions of the form {@code <class>.<static field>},</li>
 *     <li>{@code .<static method>(<arguments>)} of expressions of the form {@code <class>.<static method>(<arguments>)}, and</li>
 *     <li>{@code .<inner class>} of expressions of the form {@code <class>.<inner class>}.</li>
 * </ul>
 * The class {@code <class>} is the context for the parser. If the subexpression does not start with a dot ({@code .}),
 * then {@code <class>} is returned as parse result.
 */
public class ClassTailParser extends AbstractTailParser<TypeInfo>
{
	public ClassTailParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResult parseDot(TokenStream tokenStream, TypeInfo classType, ParseExpectation expectation) {
		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals(".");

		AbstractEntityParser<TypeInfo> fieldParser = parserToolbox.getClassFieldParser();
		AbstractEntityParser<TypeInfo> methodParser = parserToolbox.getClassMethodParser();
		AbstractEntityParser<TypeInfo> innerClassParser = parserToolbox.getInnerClassParser();
		AbstractEntityParser<TypeInfo> classObjectParser = parserToolbox.getClassObjectParser();
		if (expectation.getEvaluationType() == ParseResultType.CLASS_PARSE_RESULT) {
			return innerClassParser.parse(tokenStream, classType, expectation);
		} else {
			return ParseUtils.parse(tokenStream, classType, expectation,
				fieldParser,
				methodParser,
				innerClassParser,
				classObjectParser
			);
		}
	}

	@Override
	ParseResult parseOpeningSquareBracket(TokenStream tokenStream, TypeInfo context, ParseExpectation expectation) {
		/*
		 * If called under ConstructorParser, then this is an array construction. As we do not
		 * know, in which circumstances this method is called, the caller must handle this
		 * operator. Hence, we stop parsing here.
		 */
		return ParseResults.createClassParseResult(tokenStream.getPosition(), context);
	}

	@Override
	ParseResult createParseResult(int position, TypeInfo type) {
		return ParseResults.createClassParseResult(position, type);
	}
}
