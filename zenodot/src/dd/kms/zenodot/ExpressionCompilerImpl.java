package dd.kms.zenodot;

import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.result.CompiledObjectParseResult;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;
import java.util.Optional;

class ExpressionCompilerImpl extends AbstractParser<ObjectParseResult, ObjectParseResultExpectation> implements ExpressionCompiler
{
	private static final ObjectParseResultExpectation	PARSE_RESULT_EXPECTATION	= new ObjectParseResultExpectation().parseWholeText(true);

	private final TypeInfo	thisType;

	ExpressionCompilerImpl(String text, ParserSettings settings, TypeInfo thisType) {
		super(text, settings);
		this.thisType = thisType;
	}

	@Override
	public List<CodeCompletion> getCompletions(int caretPosition) throws ParseException {
		return getCodeCompletions(caretPosition, PARSE_RESULT_EXPECTATION).getCompletions();
	}

	@Override
	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(int caretPosition) throws ParseException {
		return getCodeCompletions(caretPosition, PARSE_RESULT_EXPECTATION).getExecutableArgumentInfo();
	}

	@Override
	public CompiledExpression compile() throws ParseException {
		TokenStream tokenStream = new TokenStream(text, -1);
		CompiledObjectParseResult compiledObjectParseResult;
		try {
			ObjectParseResult parseResult = parse(tokenStream, ParseMode.COMPILATION, PARSE_RESULT_EXPECTATION);
			if (!(parseResult instanceof CompiledObjectParseResult)) {
				throw new InternalErrorException("Parse result is not compiled");
			}
			compiledObjectParseResult = (CompiledObjectParseResult) parseResult;
		} catch (Throwable t) {
			throw new ParseException(tokenStream.getPosition(), t.getMessage(), t);
		}
		return createCompiledExpression(compiledObjectParseResult);
	}

	@Override
	ObjectParseResult doParse(TokenStream tokenStream, ParseMode parseMode, ObjectParseResultExpectation parseResultExpectation) throws AmbiguousParseResultException, CodeCompletionException, InternalParseException, InternalEvaluationException, InternalErrorException {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(InfoProvider.INDETERMINATE_VALUE, thisType);
		ParserToolbox parserToolbox  = new ParserToolbox(thisInfo, settings, parseMode);
		return parserToolbox.createExpressionParser().parse(tokenStream, thisInfo, parseResultExpectation);
	}

	private CompiledExpression createCompiledExpression(CompiledObjectParseResult compiledParseResult) {
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
