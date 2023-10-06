package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParseUtils;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;

import java.util.Arrays;
import java.util.List;

/**
 * Parses an arbitrary Java expression without binary operators. Use the {@link ExpressionParser}
 * if binary operators should be considered as well.
 */
public class SimpleExpressionParser extends AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>
{
	public SimpleExpressionParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	protected ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws CodeCompletionException, InternalErrorException, SyntaxException, EvaluationException {
		List<AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>> parsers = Arrays.asList(
			parserToolbox.createParser(LiteralParser.class),
			parserToolbox.createParser(VariableParser.class),
			parserToolbox.createParser(ObjectFieldParser.class),
			parserToolbox.createParser(ObjectMethodParser.class),
			parserToolbox.createParser(ParenthesizedExpressionParser.class),
			parserToolbox.createParser(CastParser.class),
			parserToolbox.createParser(UnqualifiedClassParser.class),
			parserToolbox.createParser(RootpackageParser.class),
			parserToolbox.createParser(ConstructorParser.class),
			parserToolbox.createParser(UnaryPrefixOperatorParser.class),
			parserToolbox.createParser(CustomHierarchyParser.class),
			parserToolbox.createParser(LambdaParser.class)
		) ;
		/*
		 * possible ambiguities:
		 *
		 * - variable name identical to field name => variable wins (rationale: field name could be qualified with this,
		 *                                            resembles scope resolution for local variables)
		 *
		 * - field name identical to name of imported class or name of class in default package  => field name wins
		 *
		 * - imported class name identical to class name in default package => imported class wins
		 */
		return ParseUtils.parse(tokenStream, contextInfo, expectation, parsers);
	}
}
