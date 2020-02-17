package dd.kms.zenodot;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.settings.ParserSettings;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Map;
import java.util.Optional;

class ExpressionCompilerImpl extends AbstractParser implements ExpressionCompiler
{
	private final TypeInfo	thisType;

	public ExpressionCompilerImpl(String text, ParserSettings settings, TypeInfo thisType) {
		super(text, settings);
		this.thisType = thisType;
	}

	@Override
	public Map<CompletionSuggestion, MatchRating> suggestCodeCompletion(int caretPosition) throws ParseException {
		return getCompletionSuggestions(caretPosition).getRatedSuggestions();
	}

	@Override
	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(int caretPosition) throws ParseException {
		return getCompletionSuggestions(caretPosition).getExecutableArgumentInfo();
	}

	@Override
	public CompiledExpression compile() throws ParseException {
		ParseOutcome parseOutcome = parse(ParseMode.COMPILATION, -1);

		if (ParseOutcomes.isCompiledParseResult(parseOutcome)) {
			CompiledObjectParseResult result = (CompiledObjectParseResult) parseOutcome;
			checkParsedWholeText(result);
			return createCompiledExpression(result);
		}
		throw createInvalidResultTypeException(parseOutcome);
	}

	@Override
	ParseOutcome doParse(TokenStream tokenStream, ParseMode parseMode) {
		ObjectInfo thisInfo = InfoProvider.createObjectInfo(InfoProvider.INDETERMINATE_VALUE, thisType);
		ParserToolbox parserToolbox  = new ParserToolbox(thisInfo, settings, parseMode);
		return parserToolbox.getExpressionParser().parse(tokenStream, thisInfo, ParseExpectation.OBJECT);
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
