package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.debug.ParserLoggers;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * Base class of all parsers. Each parser assumes that it is called in a certain context. This is
 * either an object (described by {@link ObjectInfo}) or a class (describes by {@link TypeInfo}).<br/>
 * <br/>
 * <b>Example 1:</b> The {@link ExpressionParser} that is used to parse expressions is called in the
 * context of the object that can be referred to by {@code this}. It evaluates a field name to the value
 * of the field of {@code this} with the specified name.<br/>
 * <br/>
 * <b>Example 2:</b> The {@link ClassFieldParser} that is used to parse fields of a class is
 * called in the context of that class. It evaluates a field name to the value of the (static) field
 * of that class with the specified name.<br/>
 * <br/>
 * Every parser is not only responsible for parsing a certain part of the expression, but it also
 * has to ensure that the parsing process continues after parsing that part. To do so, it delegates
 * the work to appropriate parsers.<br/>
 * <br/>
 * <b>Example 3:</b> Consider the expression {@code String.valueOf(1.23).length()}. When we ignore
 * all the parsers that fail parsing certain subexpressions, we get the following sequence of steps:
 * <ol>
 *     <li>
 *         The {@link ExpressionParser} calls the {@link SimpleExpressionParser} to parse the expression.
 *     </li>
 *     <li>
 *         The {@link SimpleExpressionParser} calls the {@link ImportedClassParser} to parse the class {@code String}.
 *     </li>
 *     <li>
 *         After parsing the class {@code String}, the {@code ClassParser} calls the {@link ClassTailParser}
 *         to check whether the expression has been parsed completely.
 *     </li>
 *     <li>
 *         The {@code ClassTailParser} detects the dot ({@code .}) and calls the {@link ClassMethodParser} to
 *         parse the method {@code valueOf} (in the context of the class {@code String}).
 *     </li>
 *     <li>
 *         The {@code ClassMethodParser} parses the method argument {@code 1.23} using the {@code ExpressionParser}
 *         (which uses the {@code SimpleExpressionParser} which then uses the {@link LiteralParser}).
 *     </li>
 *     <li>
 *         The {@code ClassMethodParser} then determines the correct overload {@link String#valueOf(double)},
 *         evaluates it and calls the {@link ObjectTailParser} to check whether the expression has been parsed
 *         completely.
 *     </li>
 *     <li>
 *         The {@code ObjectTailParser} detects the dot ({@code .}) and calls the {@link ObjectMethodParser}
 *         to parse the method {@code length} (in the context of the return value of {@code String.valueOf(1.23)}.
 *     </li>
 *     <li>
 *         The {@code ObjectMethodParser} calls the {@code ObjectTailParser} to check whether the expression
 *         has been parsed completely.
 *     </li>
 *     <li>
 *         The {@code ObjectTailParser} detects that the expression has been parsed completely.
 *     </li>
 * </ol>
 */
public abstract class AbstractEntityParser<C>
{
	final ParserToolbox				parserToolbox;
	final ObjectInfo				thisInfo;
	private final ParserLogger logger;

	AbstractEntityParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		this.parserToolbox = parserToolbox;
		this.thisInfo = thisInfo;
		logger = parserToolbox.getSettings().getLogger();
	}

	/**
	 * Parser specific code to parse the (sub-) expression at the position of the token stream in
	 * the given context.<br/>
	 * <br/>
	 * The implementations are given a dedicated copy of the token stream, so they can do with it
	 * whatever they like.
	 */
	abstract ParseResult doParse(TokenStream tokenStream, C context, ParseExpectation expectation);

	public final ParseResult parse(TokenStream tokenStream, C context, ParseExpectation expectation) {
		logger.beginChildScope();
		log(LogLevel.INFO, "parsing at " + tokenStream);
		try {
			ParseResult parseResult = doParse(tokenStream.clone(), context, expectation);
			log(LogLevel.INFO, "parse result: " + parseResult.getResultType());
			return checkExpectations(parseResult, expectation);
		} finally {
			logger.endChildScope();
		}
	}

	/**
	 * Checks whether the parse result matches its expectations. In the basic version, only the
	 * expected evaluation type (class or object) is checked. Only the {@link ExpressionParser}
	 * overrides this method to check the allowed types additionally.<br/>
	 * <br/>
	 * The reason for this is that the expected types are sometimes not a hard restriction, but only a
	 * help for code completion.<br/>
	 * <br/>
	 * <b>Example:</b> Consider the method {@code Double#parseDouble(String)}. After parsing {@code Double.parseDouble(},
	 *          a String expression is expected. The expression {@code Double.parseDouble(0 + "1")} is
	 *          perfectly valid. Technically, the {@link ExpressionParser} parses the method argument and ultimately
	 *          verifies that the argument is a String. However, for parsing {@code 0} and {@code "1"} it uses the
	 *          {@link SimpleExpressionParser}. To allow them to return good completion suggestions,
	 *          this parser is told to expect a String expression. However, parsing {@code 0} must not fail
	 *          due to this expectation.
	 */
	ParseResult checkExpectations(ParseResult parseResult, ParseExpectation expectation) {
		ParseResultType parseResultType = parseResult.getResultType();
		if (expectation.getEvaluationType() == ParseResultType.OBJECT_PARSE_RESULT && parseResultType == ParseResultType.CLASS_PARSE_RESULT) {
			String message = "Expected an object, but found class " + ((ClassParseResult) parseResult).getType();
			log(LogLevel.ERROR, message);
			return ParseResults.createParseError(parseResult.getPosition(), message, ErrorPriority.RIGHT_PARSER);
		}
		if (expectation.getEvaluationType() == ParseResultType.CLASS_PARSE_RESULT && parseResultType == ParseResultType.OBJECT_PARSE_RESULT) {
			String message = "Expected a class, but found object " + ((ObjectParseResult) parseResult).getObjectInfo().getObject();
			log(LogLevel.ERROR, message);
			return ParseResults.createParseError(parseResult.getPosition(), message, ErrorPriority.RIGHT_PARSER);
		}

		return parseResult;
	}

	/**
	 * This method is intended to be called from any parser to inform the logger about the current parsing progress.
	 */
	void log(LogLevel logLevel, String message) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		final String suffix;
		if (stackTraceElements.length > 2) {
			StackTraceElement element = stackTraceElements[2];
			suffix = " (" + element.getClassName() + "." + element.getMethodName() + ":" + element.getLineNumber() + ")";
		} else {
			suffix = "";
		}
		logger.log(ParserLoggers.createLogEntry(logLevel, getClass().getSimpleName(), message + suffix));
	}
}
