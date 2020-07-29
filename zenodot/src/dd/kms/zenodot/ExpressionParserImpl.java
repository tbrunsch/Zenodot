package dd.kms.zenodot;

import dd.kms.zenodot.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.flowcontrol.EvaluationException;
import dd.kms.zenodot.flowcontrol.InternalErrorException;
import dd.kms.zenodot.flowcontrol.SyntaxException;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;
import java.util.Optional;

class ExpressionParserImpl extends AbstractParser<ObjectParseResult, ObjectParseResultExpectation> implements ExpressionParser
{
	private static final ObjectParseResultExpectation	PARSE_RESULT_EXPECTATION	= new ObjectParseResultExpectation().parseWholeText(true);

	ExpressionParserImpl(String text, ParserSettings settings) {
		super(text, settings);
	}

	@Override
	public List<CodeCompletion> getCompletions(ObjectInfo thisValue, int caretPosition) throws ParseException {
		return getCodeCompletions(thisValue, caretPosition, PARSE_RESULT_EXPECTATION).getCompletions();
	}

	@Override
	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(ObjectInfo thisValue, int caretPosition) throws ParseException {
		return getCodeCompletions(thisValue, caretPosition, PARSE_RESULT_EXPECTATION).getExecutableArgumentInfo();
	}

	@Override
	public ObjectInfo evaluate(ObjectInfo thisValue) throws ParseException {
		TokenStream tokenStream = new TokenStream(text, -1);
		ObjectParseResult parseResult;
		try {
			parseResult = parse(tokenStream, thisValue, PARSE_RESULT_EXPECTATION);
		} catch (Throwable t) {
			throw new ParseException(tokenStream.getPosition(), t.getMessage(), t);
		}
		return settings.isEnableDynamicTyping()
			? parseResult.getObjectInfo()
			: parseResult.evaluate(thisValue, thisValue);
	}

	@Override
	public CompiledExpression compile(ObjectInfo thisValue) throws ParseException {
		TokenStream tokenStream = new TokenStream(text, -1);
		ObjectParseResult parseResult;
		try {
			parseResult = parse(tokenStream, thisValue, PARSE_RESULT_EXPECTATION);
		} catch (Throwable t) {
			throw new ParseException(tokenStream.getPosition(), t.getMessage(), t);
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
			public TypeInfo getResultType() {
				return compiledParseResult.getObjectInfo().getDeclaredType();
			}

			@Override
			public ObjectInfo evaluate(ObjectInfo thisValue) throws Exception {
				return compiledParseResult.evaluate(thisValue, thisValue);
			}
		};
	}
}
