package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.result.ParseResult;
import dd.kms.zenodot.api.settings.CompletionMode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.flowcontrol.*;
import dd.kms.zenodot.impl.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.impl.tokenizer.CompletionInfo;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.utils.ParserToolbox;

/**
 * Base class of all parsers. Each parser assumes that it is called in a certain context. This is
 * either an object (described by {@link ObjectInfo}) or a class (describes by {@link Class<?>}).<br/>
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
 *         The {@link SimpleExpressionParser} calls the {@link UnqualifiedClassParser} to parse the class {@code String}.
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
public abstract class AbstractParser<C, T extends ParseResult, S extends ParseResultExpectation<T>>
{
	final ParserToolbox			parserToolbox;

	private boolean				parsed;
	private ParserConfidence	confidence		= ParserConfidence.WRONG_PARSER;

	AbstractParser(ParserToolbox parserToolbox) {
		this.parserToolbox = parserToolbox;
	}

	/**
	 * Parser specific code to parse the (sub-) expression at the position of the token stream in
	 * the given context.<br/>
	 * <br/>
	 * The implementations are given a dedicated copy of the token stream, so they can do with it
	 * whatever they like.
	 */
	abstract ParseResult doParse(TokenStream tokenStream, C context, S expectation) throws CodeCompletionException, SyntaxException, InternalErrorException, EvaluationException;

	public final T parse(TokenStream tokenStream, C context, S expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		InternalLogger logger = getLogger();
		logger.beginChildScope();
		log(LogLevel.INFO, "parsing at " + tokenStream);
		try {
			if (parsed) {
				throw new InternalErrorException("Parsers must not be reused for parsing");
			}
			parsed = true;
			ParseResult parseResult = doParse(tokenStream, context, expectation);
			return expectation.check(tokenStream, parseResult, parserToolbox.getObjectInfoProvider());
		} catch (InternalErrorException | EvaluationException | SyntaxException e) {
			logger.log(getClass(), e, false);
			throw e;
		} catch (CodeCompletionException e) {
			throw e;
		} catch (Throwable t) {
			String error = ParseUtils.formatException(t, new StringBuilder()).toString();
			EvaluationException e = new EvaluationException(error, t);
			logger.log(getClass(), e, false);
			throw e;
		} finally {
			logger.endChildScope();
 		}
	}

	public InternalLogger getLogger() {
		return parserToolbox.getLogger();
	}

	public ParserConfidence getConfidence() {
		return confidence;
	}

	void log(LogLevel logLevel, String message) {
		getLogger().log(getClass(), logLevel, message);
	}

	void increaseConfidence(ParserConfidence newConfidence) throws InternalErrorException {
		if (confidence.compareTo(newConfidence) > 0) {
			throw new InternalErrorException("Trying to increase confidence from " + confidence + " to lower confidence " + newConfidence);
		}
		confidence = newConfidence;
	}

	int getInsertionBegin(CompletionInfo info) {
		return info.getTokenTextStartPosition();
	}

	int getInsertionEnd(CompletionInfo info) {
		ParserSettings settings = parserToolbox.getSettings();
		CompletionMode completionMode = settings.getCompletionMode();
		switch (completionMode) {
			case COMPLETE_AND_REPLACE_UNTIL_CARET:
				return info.getCaretPosition();
			case COMPLETE_UNTIL_CARET_REPLACE_WHOLE_WORDS:
			case COMPLETE_AND_REPLACE_WHOLE_WORDS:
				return info.getTokenTextEndPosition();
			default:
				throw new IllegalStateException("Unsupported completion mode: " + completionMode);
		}
	}

	String getTextToComplete(CompletionInfo info) {
		ParserSettings settings = parserToolbox.getSettings();
		CompletionMode completionMode = settings.getCompletionMode();
		switch (completionMode) {
			case COMPLETE_AND_REPLACE_UNTIL_CARET:
			case COMPLETE_UNTIL_CARET_REPLACE_WHOLE_WORDS:
				return info.getTokenTextUntilCaret();
			case COMPLETE_AND_REPLACE_WHOLE_WORDS:
				return info.getTokenText();
			default:
				throw new IllegalStateException("Unsupported completion mode: " + completionMode);
		}
	}
}
