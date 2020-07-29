package dd.kms.zenodot.impl;

import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.api.result.ObjectParseResult;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParserToolbox;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.util.List;
import java.util.Optional;

public class ExpressionParserImpl extends AbstractParser<ObjectParseResult, ObjectParseResultExpectation> implements ExpressionParser
{
	private static final ObjectParseResultExpectation	PARSE_RESULT_EXPECTATION	= new ObjectParseResultExpectation().parseWholeText(true);

	public ExpressionParserImpl(String text, ParserSettings settings) {
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
