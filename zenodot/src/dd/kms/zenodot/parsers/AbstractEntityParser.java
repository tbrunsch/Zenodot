package dd.kms.zenodot.parsers;

import dd.kms.zenodot.ParserToolbox;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLogEntry;
import dd.kms.zenodot.debug.ParserLoggerIF;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

public abstract class AbstractEntityParser<C>
{
	final ParserToolbox				parserToolbox;
	final ObjectInfo				thisInfo;
	private final ParserLoggerIF	logger;

	AbstractEntityParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		this.parserToolbox = parserToolbox;
		this.thisInfo = thisInfo;
		logger = parserToolbox.getSettings().getLogger();
	}

	abstract ParseResultIF doParse(TokenStream tokenStream, C context, ParseExpectation expectation);

	public final ParseResultIF parse(TokenStream tokenStream, C context, ParseExpectation expectation) {
		logger.beginChildScope();
		log(LogLevel.INFO, "parsing at " + tokenStream);
		try {
			ParseResultIF parseResult = doParse(tokenStream.clone(), context, expectation);
			log(LogLevel.INFO, "parse result: " + parseResult.getResultType());
			return checkExpectations(parseResult, expectation);
		} finally {
			logger.endChildScope();
		}
	}

	/**
	 * Checks whether the parse result matches its expectations. In the basic version, only the
	 * expected evaluation type (class or object) is checked. Only the {@link RootParser}
	 * overrides this method to check the allowed types additionally.
	 *
	 * The reason for this that the expected types are sometimes not a hard restriction, but only a
	 * help for code completion.
	 *
	 * Example: Consider the method {@link Double#parseDouble(String)}. After parsing {@code Double.parseDouble(},
	 *          a String expression is expected. The expression {@code Double.parseDouble(0 + "1")} is
	 *          perfectly valid. Technically, the {@link RootParser} is used to parse the
	 *          method argument and ultimately verifies that the argument is a String. However, for parsing
	 *          0 and "1", it uses the {@link ExpressionParser}. To allow them to return good completion suggestions,
	 *          these parsers are told to expect a String expression. However, parsing the 0 must not fail
	 *          due to this expectation.
	 */
	ParseResultIF checkExpectations(ParseResultIF parseResult, ParseExpectation expectation) {
		ParseResultType parseResultType = parseResult.getResultType();
		if (expectation.getEvaluationType() == ParseResultType.OBJECT_PARSE_RESULT && parseResultType == ParseResultType.CLASS_PARSE_RESULT) {
			String message = "Expected an object, but found class " + ((ClassParseResult) parseResult).getType();
			log(LogLevel.ERROR, message);
			return new ParseError(parseResult.getPosition(), message, ParseError.ErrorType.SEMANTIC_ERROR);
		}
		if (expectation.getEvaluationType() == ParseResultType.CLASS_PARSE_RESULT && parseResultType == ParseResultType.OBJECT_PARSE_RESULT) {
			String message = "Expected a class, but found object " + ((ObjectParseResult) parseResult).getObjectInfo().getObject();
			log(LogLevel.ERROR, message);
			return new ParseError(parseResult.getPosition(), message, ParseError.ErrorType.SEMANTIC_ERROR);
		}

		return parseResult;
	}

	void log(LogLevel logLevel, String message) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		final String suffix;
		if (stackTraceElements.length > 2) {
			StackTraceElement element = stackTraceElements[2];
			suffix = " (" + element.getClassName() + "." + element.getMethodName() + ":" + element.getLineNumber() + ")";
		} else {
			suffix = "";
		}
		logger.log(new ParserLogEntry(logLevel, getClass().getSimpleName(), message + suffix));
	}
}