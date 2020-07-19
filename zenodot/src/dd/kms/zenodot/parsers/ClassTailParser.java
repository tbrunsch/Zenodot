package dd.kms.zenodot.parsers;

import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ClassParseResultExpectation;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.result.ClassParseResult;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Arrays;
import java.util.List;

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
public class ClassTailParser<T extends ParseResult, S extends ParseResultExpectation<T>> extends AbstractTailParser<TypeInfo, T, S>
{
	public ClassTailParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ParseResult parseDot(TokenStream tokenStream, TypeInfo classType, S expectation) throws AmbiguousParseResultException, CodeCompletionException, InternalParseException, InternalEvaluationException, InternalErrorException {
		if (expectation instanceof ClassParseResultExpectation) {
			InnerClassParser<ClassParseResult, ClassParseResultExpectation> innerClassParser = parserToolbox.createParser(InnerClassParser.class);
			return innerClassParser.parse(tokenStream, classType, (ClassParseResultExpectation) expectation);
		} else if (expectation instanceof ObjectParseResultExpectation) {
			List<AbstractParser<TypeInfo, ObjectParseResult, ObjectParseResultExpectation>> parsers = Arrays.asList(
				parserToolbox.createParser(ClassFieldParser.class),
				parserToolbox.createParser(ClassMethodParser.class),
				parserToolbox.createParser(InnerClassParser.class),
				parserToolbox.createParser(ClassObjectParser.class)
			);
			// possible ambiguities: field name identical to inner class name => field wins
			return ParseUtils.parse(tokenStream, classType, (ObjectParseResultExpectation) expectation, parsers);
		} else {
			throw new InternalErrorException("Class tail parser does not meet expectations of type '" + expectation.getClass().getSimpleName() + "'");
		}
	}

	@Override
	ParseResult parseOpeningSquareBracket(TokenStream tokenStream, TypeInfo context, S expectation) {
		/*
		 * If called under ConstructorParser, then this is an array construction. As we do not
		 * know, in which circumstances this method is called, the caller must handle this
		 * operator. Hence, we stop parsing here.
		 */
		return createParseResult(context);
	}

	@Override
	ParseResult createParseResult(TypeInfo type) {
		return ParseResults.createClassParseResult(type);
	}
}
