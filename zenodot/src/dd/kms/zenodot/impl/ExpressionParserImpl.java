package dd.kms.zenodot.impl;

import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.result.ObjectParseResult;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParserToolbox;
import dd.kms.zenodot.impl.wrappers.InfoProvider;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

import java.util.List;
import java.util.Optional;

public class ExpressionParserImpl extends AbstractParser<ObjectParseResult, ObjectParseResultExpectation> implements ExpressionParser
{
	private static final ObjectParseResultExpectation	PARSE_RESULT_EXPECTATION	= new ObjectParseResultExpectation().parseWholeText(true);

	public ExpressionParserImpl(ParserSettings settings) {
		super(settings);
	}

	@Override
	public List<CodeCompletion> getCompletions(String text, int caretPosition, Class<?> thisType) throws ParseException {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(InfoProvider.INDETERMINATE_VALUE, thisType);
		return getCodeCompletions(text, caretPosition, thisInfo, PARSE_RESULT_EXPECTATION).getCompletions();
	}

	@Override
	public List<CodeCompletion> getCompletions(String text, int caretPosition, Object thisValue) throws ParseException {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(thisValue);
		return getCodeCompletions(text, caretPosition, thisInfo, PARSE_RESULT_EXPECTATION).getCompletions();
	}

	@Override
	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition, Class<?> thisType) throws ParseException {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(InfoProvider.INDETERMINATE_VALUE, thisType);
		return getCodeCompletions(text, caretPosition, thisInfo, PARSE_RESULT_EXPECTATION).getExecutableArgumentInfo();
	}

	@Override
	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition, Object thisValue) throws ParseException {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(thisValue);
		return getCodeCompletions(text, caretPosition, thisInfo, PARSE_RESULT_EXPECTATION).getExecutableArgumentInfo();
	}

	@Override
	public Object evaluate(String expression, Object thisValue) throws ParseException {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(thisValue);
		TokenStream tokenStream = new TokenStream(expression, -1);
		ObjectParseResult parseResult;
		try {
			parseResult = parse(tokenStream, thisInfo, PARSE_RESULT_EXPECTATION);
		} catch (Throwable t) {
			throw new ParseException(tokenStream, t.getMessage(), t);
		}
		if (settings.getEvaluationMode() == EvaluationMode.DYNAMIC_TYPING) {
			// everything has already been evaluated
			return parseResult.getObjectInfo().getObject();
		}
		return parseResult.evaluate(thisInfo, thisInfo).getObject();
	}

	@Override
	public CompiledExpression compile(String expression, Class<?> thisClass) throws ParseException {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(InfoProvider.INDETERMINATE_VALUE, thisClass);
		return doCompile(expression, thisInfo);
	}

	@Override
	public CompiledExpression compile(String expression, Object thisValue) throws ParseException {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(thisValue);
		return doCompile(expression, thisInfo);
	}

	private CompiledExpression doCompile(String expression, ObjectInfo thisInfo) throws ParseException {
		TokenStream tokenStream = new TokenStream(expression, -1);
		ObjectParseResult parseResult;
		try {
			parseResult = parse(tokenStream, thisInfo, PARSE_RESULT_EXPECTATION);
		} catch (Throwable t) {
			throw new ParseException(tokenStream, t.getMessage(), t);
		}
		return createCompiledExpression(parseResult);
	}

	@Override
	ObjectParseResult doParse(TokenStream tokenStream, ParserToolbox parserToolbox, ObjectParseResultExpectation parseResultExpectation) throws InternalErrorException, EvaluationException, CodeCompletionException, SyntaxException {
		return parserToolbox.createExpressionParser().parse(tokenStream, parserToolbox.getThisInfo(), parseResultExpectation);
	}

	private CompiledExpression createCompiledExpression(ObjectParseResult compiledParseResult) {
		return new CompiledExpression()
		{
			@Override
			public Class<?> getResultType() {
				return compiledParseResult.getObjectInfo().getDeclaredType();
			}

			@Override
			public Object evaluate(Object thisValue) throws Exception {
				ObjectInfo thisInfo = InfoProvider.createObjectInfo(thisValue);
				return compiledParseResult.evaluate(thisInfo, thisInfo).getObject();
			}
		};
	}
}
