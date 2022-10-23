package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ClassParseResultExpectation;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.impl.result.ClassParseResult;
import dd.kms.zenodot.impl.result.ObjectParseResult;
import dd.kms.zenodot.impl.result.ParseResult;
import dd.kms.zenodot.impl.result.ParseResults;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.utils.ParserToolbox;

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
public class ClassTailParser<T extends ParseResult, S extends ParseResultExpectation<T>> extends AbstractTailParser<Class<?>, T, S>
{
	public ClassTailParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ParseResult parseDot(TokenStream tokenStream, Class<?> classType, S expectation) throws CodeCompletionException, SyntaxException, EvaluationException, InternalErrorException {
		if (expectation instanceof ClassParseResultExpectation) {
			InnerClassParser<ClassParseResult, ClassParseResultExpectation> innerClassParser = parserToolbox.createParser(InnerClassParser.class);
			return innerClassParser.parse(tokenStream, classType, (ClassParseResultExpectation) expectation);
		} else if (expectation instanceof ObjectParseResultExpectation) {
			List<AbstractParser<Class<?>, ObjectParseResult, ObjectParseResultExpectation>> parsers = Arrays.asList(
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
	ParseResult parseOpeningSquareBracket(TokenStream tokenStream, Class<?> context, S expectation) {
		/*
		 * If called under ConstructorParser, then this is an array construction. As we do not
		 * know, in which circumstances this method is called, the caller must handle this
		 * operator. Hence, we stop parsing here.
		 */
		return createParseResult(tokenStream, context);
	}

	@Override
	ParseResult createParseResult(TokenStream tokenStream, Class<?> type) {
		return ParseResults.createClassParseResult(type);
	}
}
