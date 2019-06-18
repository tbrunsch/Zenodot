package dd.kms.zenodot.parsers;

import dd.kms.zenodot.result.ParseOutcome;
import dd.kms.zenodot.result.ParseOutcomes;
import dd.kms.zenodot.result.ParseResultType;
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
	ParseOutcome parseDot(TokenStream tokenStream, TypeInfo classType, ParseExpectation expectation) {
		Token characterToken = tokenStream.readCharacterUnchecked();
		assert characterToken.getValue().equals(".");

		AbstractParser<TypeInfo> fieldParser = parserToolbox.getClassFieldParser();
		AbstractParser<TypeInfo> methodParser = parserToolbox.getClassMethodParser();
		AbstractParser<TypeInfo> innerClassParser = parserToolbox.getInnerClassParser();
		AbstractParser<TypeInfo> classObjectParser = parserToolbox.getClassObjectParser();
		if (expectation.getResultType() == ParseResultType.CLASS) {
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
	ParseOutcome parseOpeningSquareBracket(TokenStream tokenStream, TypeInfo context, ParseExpectation expectation) {
		/*
		 * If called under ConstructorParser, then this is an array construction. As we do not
		 * know, in which circumstances this method is called, the caller must handle this
		 * operator. Hence, we stop parsing here.
		 */
		return ParseOutcomes.createClassParseResult(tokenStream.getPosition(), context);
	}

	@Override
	ParseOutcome createParseOutcome(int position, TypeInfo type) {
		return ParseOutcomes.createClassParseResult(position, type);
	}
}
