package dd.kms.zenodot.impl.parsers;

import com.google.common.collect.Lists;
import dd.kms.zenodot.api.settings.parsers.AdditionalParserSettings;
import dd.kms.zenodot.api.settings.parsers.ParserType;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.CallerContext;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParseUtils;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;

import java.util.ArrayList;
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
		// predefined parser classes
		List<Class<? extends AbstractParser>> parserClasses = Lists.newArrayList(
			LiteralParser.class,
			VariableParser.class,
			ObjectFieldParser.class,
			ObjectMethodParser.class,
			ParenthesizedExpressionParser.class,
			CastParser.class,
			UnqualifiedClassParser.class,
			RootpackageParser.class,
			ConstructorParser.class,
			UnaryPrefixOperatorParser.class,
			LambdaParser.class
		);

		// additional parser classes
		List<AdditionalParserSettings> additionalParserSettings = parserToolbox.getSettings().getAdditionalParserSettings();
		additionalParserSettings.stream()
			.filter(settings -> settings.getParserType() == ParserType.ROOT_OBJECT_PARSER)
			.forEach(settings -> parserClasses.add(settings.getParserClass()));

		List<AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation>> parsers = new ArrayList<>();
		CallerContext callerContext = getCallerContext();
		for (Class<? extends AbstractParser> parserClass : parserClasses) {
			AbstractParser<ObjectInfo, ObjectParseResult, ObjectParseResultExpectation> parser = parserToolbox.createParser(parserClass);
			parser.setCallerContext(callerContext);
			parsers.add(parser);
		}

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
