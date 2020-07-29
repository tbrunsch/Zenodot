package dd.kms.zenodot.parsers;

import dd.kms.zenodot.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.flowcontrol.EvaluationException;
import dd.kms.zenodot.flowcontrol.InternalErrorException;
import dd.kms.zenodot.flowcontrol.SyntaxException;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

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
	ObjectParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws CodeCompletionException, InternalErrorException, SyntaxException, EvaluationException {
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
			parserToolbox.createParser(CustomHierarchyParser.class)
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
