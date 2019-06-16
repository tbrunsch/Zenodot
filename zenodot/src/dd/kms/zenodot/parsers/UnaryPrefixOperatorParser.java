package dd.kms.zenodot.parsers;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.tokenizer.UnaryOperator;
import dd.kms.zenodot.utils.EvaluationMode;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataProviders.OperatorResultProvider;
import dd.kms.zenodot.utils.dataProviders.OperatorResultProvider.OperatorException;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Parses prefix unary operators applied to an expression in the context of {@code this}. The following
 * expressions can currently be parsed:
 * <ul>
 *     <li>{@code ++<expression>}</li>
 *     <li>{@code --<expression>}</li>
 *     <li>{@code +<expression>}</li>
 *     <li>{@code -<expression>}</li>
 *     <li>{@code !<expression>}</li>
 *     <li>{@code ~<expression>}</li>
 * </ul>
 * Note that the postfix operators {@code <expression>++} and {@code <expression>--} are not supported.
 */
public class UnaryPrefixOperatorParser extends AbstractEntityParser<ObjectInfo>
{
	private static final Map<UnaryOperator, OperatorImplementation>	OPERATOR_IMPLEMENTATIONS = ImmutableMap.<UnaryOperator, OperatorImplementation>builder()
		.put(UnaryOperator.INCREMENT, 	OperatorResultProvider::getIncrementInfo)
		.put(UnaryOperator.DECREMENT, 	OperatorResultProvider::getDecrementInfo)
		.put(UnaryOperator.PLUS, 		OperatorResultProvider::getPlusInfo)
		.put(UnaryOperator.MINUS, 		OperatorResultProvider::getMinusInfo)
		.put(UnaryOperator.LOGICAL_NOT,	OperatorResultProvider::getLogicalNotInfo)
		.put(UnaryOperator.BITWISE_NOT,	OperatorResultProvider::getBitwiseNotInfo)
		.build();

	public UnaryPrefixOperatorParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseOutcome doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		Token operatorToken = tokenStream.readUnaryOperatorUnchecked();
		if (operatorToken == null) {
			log(LogLevel.ERROR, "expected unary operator");
			return ParseOutcomes.createParseError(tokenStream.getPosition(), "Expression does not start with an unary operator", ErrorPriority.WRONG_PARSER);
		} else if (operatorToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available");
			return CompletionSuggestions.none(tokenStream.getPosition());
		}
		UnaryOperator operator = UnaryOperator.getValue(operatorToken.getValue());

		ParseOutcome parseOutcome = parserToolbox.getSimpleExpressionParser().parse(tokenStream, contextInfo, expectation);

		Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(parseOutcome, expectation, ErrorPriority.RIGHT_PARSER);
		if (parseOutcomeForPropagation.isPresent()) {
			return parseOutcomeForPropagation.get();
		}

		if (parserToolbox.getEvaluationMode() == EvaluationMode.COMPILED) {
			return compile(parseOutcome, operator);
		}

		ObjectParseResult expressionParseResult = (ObjectParseResult) parseOutcome;
		int parsedToPosition = expressionParseResult.getPosition();
		ObjectInfo expressionInfo = expressionParseResult.getObjectInfo();

		ObjectInfo operatorResult;
		try {
			operatorResult = applyOperator(expressionInfo, operator);
			log(LogLevel.SUCCESS, "applied operator successfully");
		} catch (OperatorException e) {
			log(LogLevel.ERROR, "applying operator failed: " + e.getMessage());
			return ParseOutcomes.createParseError(parsedToPosition, e.getMessage(), ErrorPriority.RIGHT_PARSER);
		}
		return ParseOutcomes.createObjectParseResult(parsedToPosition, operatorResult);
	}

	private ObjectInfo applyOperator(ObjectInfo objectInfo, UnaryOperator operator) throws OperatorException {
		return applyOperator(objectInfo, operator, parserToolbox.getOperatorResultProvider());
	}

	private static ObjectInfo applyOperator(ObjectInfo objectInfo, UnaryOperator operator, OperatorResultProvider operatorResultProvider) throws OperatorException {
		return OPERATOR_IMPLEMENTATIONS.get(operator).apply(operatorResultProvider, objectInfo);
	}

	private ParseOutcome compile(ParseOutcome expressionParseOutcome, UnaryOperator operator) {
		if (!ParseOutcomes.isCompiledParseResult(expressionParseOutcome)) {
			return expressionParseOutcome;
		}
		CompiledObjectParseResult compiledExpressionParseResult = (CompiledObjectParseResult) expressionParseOutcome;
		return new CompiledUnaryPrefixOperatorParseResult(compiledExpressionParseResult, operator);
	}

	@FunctionalInterface
	private interface OperatorImplementation
	{
		ObjectInfo apply(OperatorResultProvider operatorResultProvider, ObjectInfo objectInfo) throws OperatorException;
	}

	private static class CompiledUnaryPrefixOperatorParseResult extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult	compiledExpressionParseResult;
		private final UnaryOperator							operator;

		private CompiledUnaryPrefixOperatorParseResult(CompiledObjectParseResult compiledExpressionParseResult, UnaryOperator operator) {
			super(compiledExpressionParseResult.getPosition(), compiledExpressionParseResult.getObjectInfo());
			this.compiledExpressionParseResult = compiledExpressionParseResult;
			this.operator = operator;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) throws Exception {
			ObjectInfo expressionInfo = compiledExpressionParseResult.evaluate(thisInfo, context);
			return applyOperator(expressionInfo, operator, OPERATOR_RESULT_PROVIDER);
		}
	}
}
