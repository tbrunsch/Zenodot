package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorType;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.tokenizer.UnaryOperator;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.dataProviders.OperatorResultProvider;
import dd.kms.zenodot.utils.dataProviders.OperatorResultProvider.OperatorException;
import dd.kms.zenodot.utils.ParseUtils;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class UnaryPrefixOperatorParser extends AbstractEntityParser<ObjectInfo>
{
	private static final Map<UnaryOperator, OperatorImplementationIF>	OPERATOR_IMPLEMENTATIONS = ImmutableMap.<UnaryOperator, OperatorImplementationIF>builder()
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
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		Token operatorToken = tokenStream.readUnaryOperatorUnchecked();
		if (operatorToken == null) {
			log(LogLevel.ERROR, "expected unary operator");
			return new ParseError(tokenStream.getPosition(), "Expression does not start with an unary operator", ErrorType.WRONG_PARSER);
		} else if (operatorToken.isContainsCaret()) {
			log(LogLevel.INFO, "no completion suggestions available");
			return CompletionSuggestions.none(tokenStream.getPosition());
		}
		UnaryOperator operator = UnaryOperator.getValue(operatorToken.getValue());

		ParseResultIF parseResult = parserToolbox.getExpressionParser().parse(tokenStream, contextInfo, expectation);

		if (ParseUtils.propagateParseResult(parseResult, expectation)) {
			return parseResult;
		}

		ObjectParseResult expressionParseResult = (ObjectParseResult) parseResult;
		int parsedToPosition = expressionParseResult.getPosition();
		ObjectInfo expressionInfo = expressionParseResult.getObjectInfo();

		ObjectInfo operatorResult;
		try {
			operatorResult = applyOperator(expressionInfo, operator);
			log(LogLevel.SUCCESS, "applied operator successfully");
		} catch (OperatorException e) {
			log(LogLevel.ERROR, "applying operator failed: " + e.getMessage());
			return new ParseError(parsedToPosition, e.getMessage(), ErrorType.SEMANTIC_ERROR);
		}
		return new ObjectParseResult(parsedToPosition, operatorResult);
	}

	private ObjectInfo applyOperator(ObjectInfo objectInfo, UnaryOperator operator) throws OperatorException {
		return OPERATOR_IMPLEMENTATIONS.get(operator).apply(parserToolbox.getOperatorResultProvider(), objectInfo);
	}

	@FunctionalInterface
	private interface OperatorImplementationIF
	{
		ObjectInfo apply(OperatorResultProvider operatorResultProvider, ObjectInfo objectInfo) throws OperatorException;
	}
}
