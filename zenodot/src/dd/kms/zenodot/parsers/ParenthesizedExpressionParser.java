package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.EvaluationMode;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.Optional;

/**
 * Parses expressions of the form {@code (<expression>)} in the context of {@code this}.
 */
public class ParenthesizedExpressionParser extends AbstractEntityParser<ObjectInfo>
{
	public ParenthesizedExpressionParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseOutcome doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '(') {
			log(LogLevel.ERROR, "missing '('");
			return ParseOutcomes.createParseError(position, "Expected opening parenthesis '('", ErrorPriority.WRONG_PARSER);
		}

		ParseOutcome expressionParseOutcome = parserToolbox.getExpressionParser().parse(tokenStream, contextInfo, expectation);

		Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(expressionParseOutcome, expectation, ErrorPriority.POTENTIALLY_RIGHT_PARSER);
		if (parseOutcomeForPropagation.isPresent()) {
			return parseOutcomeForPropagation.get();
		}

		ObjectParseResult parseResult = (ObjectParseResult) expressionParseOutcome;
		int parsedToPosition = parseResult.getPosition();
		ObjectInfo objectInfo = parseResult.getObjectInfo();
		tokenStream.moveTo(parsedToPosition);

		characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != ')') {
			log(LogLevel.ERROR, "missing ')'");
			return ParseOutcomes.createParseError(position, "Expected closing parenthesis ')'", ErrorPriority.RIGHT_PARSER);
		}
		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available at " + tokenStream);
			return CompletionSuggestions.none(tokenStream.getPosition());
		}
		ParseOutcome parseOutcome = parserToolbox.getObjectTailParser().parse(tokenStream, objectInfo, expectation);
		return parserToolbox.getEvaluationMode() == EvaluationMode.COMPILED
				? compile(parseOutcome, expressionParseOutcome)
				: parseOutcome;
	}

	private ParseOutcome compile(ParseOutcome tailParseOutcome, ParseOutcome expressionParseOutcome) {
		if (!ParseOutcomes.isCompiledParseResult(tailParseOutcome)) {
			return tailParseOutcome;
		}
		CompiledObjectParseResult compiledTailParseResult = (CompiledObjectParseResult) tailParseOutcome;
		CompiledObjectParseResult compiledExpressionParseResult = (CompiledObjectParseResult) expressionParseOutcome;
		return new CompiledParenthesizedExpressionParseResult(compiledTailParseResult, compiledExpressionParseResult);
	}

	private static class CompiledParenthesizedExpressionParseResult extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult	compiledTailParseResult;
		private final CompiledObjectParseResult	compiledExpressionParseResult;

		CompiledParenthesizedExpressionParseResult(CompiledObjectParseResult compiledTailParseResult, CompiledObjectParseResult compiledExpressionParseResult) {
			super(compiledTailParseResult.getPosition(), compiledTailParseResult.getObjectInfo());
			this.compiledTailParseResult = compiledTailParseResult;
			this.compiledExpressionParseResult = compiledExpressionParseResult;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) throws Exception {
			ObjectInfo expressionInfo = compiledExpressionParseResult.evaluate(thisInfo, context);
			return compiledTailParseResult.evaluate(thisInfo, expressionInfo);
		}
	}
}
