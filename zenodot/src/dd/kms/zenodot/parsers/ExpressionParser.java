package dd.kms.zenodot.parsers;

import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.tokenizer.BinaryOperator;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseMode;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataProviders.OperatorResultProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static dd.kms.zenodot.utils.dataProviders.OperatorResultProvider.OperatorException;

/**
 * Parses arbitrary Java expressions including binary operators by using the
 * {@link SimpleExpressionParser} for parsing the operands.
 */
public class ExpressionParser extends AbstractEntityParser<ObjectInfo>
{
	private static final Map<BinaryOperator, OperatorImplementation>	OPERATOR_IMPLEMENTATIONS = ImmutableMap.<BinaryOperator, OperatorImplementation>builder()
		.put(BinaryOperator.MULTIPLY, 					OperatorResultProvider::getMultiplicationInfo)
		.put(BinaryOperator.DIVIDE, 					OperatorResultProvider::getDivisionInfo)
		.put(BinaryOperator.MODULO,						OperatorResultProvider::getModuloInfo)
		.put(BinaryOperator.ADD_OR_CONCAT, 				OperatorResultProvider::getAddOrConcatInfo)
		.put(BinaryOperator.SUBTRACT, 					OperatorResultProvider::getSubtractionInfo)
		.put(BinaryOperator.LEFT_SHIFT,					OperatorResultProvider::getLeftShiftInfo)
		.put(BinaryOperator.RIGHT_SHIFT, 				OperatorResultProvider::getRightShiftInfo)
		.put(BinaryOperator.UNSIGNED_RIGHT_SHIFT, 		OperatorResultProvider::getUnsignedRightShiftInfo)
		.put(BinaryOperator.LESS_THAN, 					OperatorResultProvider::getLessThanInfo)
		.put(BinaryOperator.LESS_THAN_OR_EQUAL_TO,		OperatorResultProvider::getLessThanOrEqualToInfo)
		.put(BinaryOperator.GREATER_THAN, 				OperatorResultProvider::getGreaterThanInfo)
		.put(BinaryOperator.GREATER_THAN_OR_EQUAL_TO,	OperatorResultProvider::getGreaterThanOrEqualToInfo)
		.put(BinaryOperator.EQUAL_TO, 					OperatorResultProvider::getEqualToInfo)
		.put(BinaryOperator.NOT_EQUAL_TO, 				OperatorResultProvider::getNotEqualToInfo)
		.put(BinaryOperator.BITWISE_AND, 				OperatorResultProvider::getBitwiseAndInfo)
		.put(BinaryOperator.BITWISE_XOR, 				OperatorResultProvider::getBitwiseXorInfo)
		.put(BinaryOperator.BITWISE_OR, 				OperatorResultProvider::getBitwiseOrInfo)
		.put(BinaryOperator.LOGICAL_AND, 				OperatorResultProvider::getLogicalAndInfo)
		.put(BinaryOperator.LOGICAL_OR, 				OperatorResultProvider::getLogicalOrInfo)
		.put(BinaryOperator.ASSIGNMENT, 				OperatorResultProvider::getAssignmentInfo)
		.build();

	private final int maxOperatorPrecedenceLevelToConsider;

	public ExpressionParser(ParserToolbox parserToolbox, ObjectInfo thisInfo, int maxOperatorPrecedenceLevelToConsider) {
		super(parserToolbox, thisInfo);
		this.maxOperatorPrecedenceLevelToConsider = maxOperatorPrecedenceLevelToConsider;
	}

	@Override
	ParseOutcome doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		log(LogLevel.INFO, "parsing first expression");
		ParseOutcome parseOutcome = parserToolbox.getSimpleExpressionParser().parse(tokenStream, contextInfo, expectation);

		Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(parseOutcome, expectation, ErrorPriority.WRONG_PARSER);
		if (parseOutcomeForPropagation.isPresent()) {
			return parseOutcomeForPropagation.get();
		}
		ObjectParseResult lhsParseResult = (ObjectParseResult) parseOutcome;
		ObjectInfo lhsInfo = lhsParseResult.getObjectInfo();
		int parsedToPosition = lhsParseResult.getPosition();
		tokenStream.moveTo(parsedToPosition);

		/*
		 * If short circuit evaluation becomes active, then we must switch to a non-evaluating contextInfo,
		 * but still check for syntax errors.
		 */

		ParserToolbox parserToolbox = this.parserToolbox;
		boolean considerOperatorResult = true;
		while (true) {
			Token operatorToken = tokenStream.readBinaryOperatorUnchecked();
			if (operatorToken == null) {
				return ParseOutcomes.createObjectParseResult(parsedToPosition, lhsInfo);
			}
			log(LogLevel.SUCCESS, "detected binary operator '" + operatorToken.getValue() + "' at " + tokenStream);

			if (operatorToken.isContainsCaret()) {
				log(LogLevel.INFO, "no completion suggestions available");
				return CompletionSuggestions.none(tokenStream.getPosition());
			}

			BinaryOperator operator = BinaryOperator.getValue(operatorToken.getValue());
			if (operator.getPrecedenceLevel() > maxOperatorPrecedenceLevelToConsider) {
				return ParseOutcomes.createObjectParseResult(parsedToPosition, lhsInfo);
			}

			switch (operator.getAssociativity()) {
				case LEFT_TO_RIGHT: {
					if (considerOperatorResult && stopCircuitEvaluation(lhsInfo, operator)) {
						parserToolbox = createToolboxWithoutEvaluation();
						considerOperatorResult = false;
					}
					parseOutcome = parserToolbox.createExpressionParser(operator.getPrecedenceLevel() - 1).parse(tokenStream, contextInfo, ParseExpectation.OBJECT);

					parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(parseOutcome, ParseExpectation.OBJECT, ErrorPriority.RIGHT_PARSER);
					if (parseOutcomeForPropagation.isPresent()) {
						return parseOutcomeForPropagation.get();
					}
					ObjectParseResult rhsParseResult = (ObjectParseResult) parseOutcome;
					ObjectInfo rhsInfo = rhsParseResult.getObjectInfo();
					parsedToPosition = rhsParseResult.getPosition();
					tokenStream.moveTo(parsedToPosition);

					try {
						// Check syntax even if result of operator is not considered because of short circuit evaluation
						ObjectInfo operatorResult = applyOperator(parserToolbox, lhsInfo, rhsInfo, operator);
						if (considerOperatorResult) {
							lhsInfo = operatorResult;
						}
						log(LogLevel.SUCCESS, "applied operator successfully");
					} catch (OperatorException e) {
						log(LogLevel.ERROR, "applying operator failed: " + e.getMessage());
						return ParseOutcomes.createParseError(parsedToPosition, e.getMessage(), ErrorPriority.RIGHT_PARSER);
					}
					break;
				}
				case RIGHT_TO_LEFT: {
					parseOutcome = parserToolbox.createExpressionParser(operator.getPrecedenceLevel()).parse(tokenStream, contextInfo, ParseExpectation.OBJECT);

					parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(parseOutcome, ParseExpectation.OBJECT, ErrorPriority.RIGHT_PARSER);
					if (parseOutcomeForPropagation.isPresent()) {
						return parseOutcomeForPropagation.get();
					}
					ObjectParseResult rhsParseResult = (ObjectParseResult) parseOutcome;
					ObjectInfo rhsInfo = rhsParseResult.getObjectInfo();

					ObjectInfo operatorResultInfo;
					try {
						operatorResultInfo = applyOperator(parserToolbox, lhsInfo, rhsInfo, operator);
						log(LogLevel.SUCCESS, "applied operator successfully");
					} catch (OperatorException e) {
						log(LogLevel.ERROR, "applying operator failed: " + e.getMessage());
						return ParseOutcomes.createParseError(rhsParseResult.getPosition(), e.getMessage(), ErrorPriority.RIGHT_PARSER);
					}
					return ParseOutcomes.createObjectParseResult(rhsParseResult.getPosition(), operatorResultInfo);
				}
				default:
					return ParseOutcomes.createParseError(tokenStream.getPosition(), "Internal error: Unknown operator associativity: " + operator.getAssociativity(), ErrorPriority.INTERNAL_ERROR);
			}
		}
	}

	@Override
	ParseOutcome checkExpectations(ParseOutcome parseOutcome, ParseExpectation expectation) {
		parseOutcome = super.checkExpectations(parseOutcome, expectation);

		if (parseOutcome.getOutcomeType() != ParseOutcomeType.OBJECT_PARSE_RESULT) {
			// no further checks required
			return parseOutcome;
		}

		ObjectParseResult objectParseResult = (ObjectParseResult) parseOutcome;
		List<TypeInfo> allowedTypes = expectation.getAllowedTypes();
		TypeInfo resultType = parserToolbox.getObjectInfoProvider().getType(objectParseResult.getObjectInfo());
		if (allowedTypes != null && allowedTypes.stream().noneMatch(expectedResultType -> MatchRatings.isConvertibleTo(resultType, expectedResultType))) {
			String messagePrefix = "The class '" + resultType + "' is not assignable to ";
			String messageMiddle = allowedTypes.size() > 1
					? "any of the expected classes "
					: "the expected class ";
			String messageSuffix = "'" + allowedTypes.stream().map(Object::toString).collect(Collectors.joining("', '")) + "'";
			String message = messagePrefix + messageMiddle + messageSuffix;
			log(LogLevel.ERROR, message);
			return ParseOutcomes.createParseError(parseOutcome.getPosition(), message, ErrorPriority.RIGHT_PARSER);
		}

		return objectParseResult;

	}

	private ObjectInfo applyOperator(ParserToolbox context, ObjectInfo lhs, ObjectInfo rhs, BinaryOperator operator) throws OperatorException {
		return OPERATOR_IMPLEMENTATIONS.get(operator).apply(context.getOperatorResultProvider(), lhs, rhs);
	}

	private boolean stopCircuitEvaluation(ObjectInfo objectInfo, BinaryOperator operator) {
		return operator == BinaryOperator.LOGICAL_AND	&& Boolean.FALSE.equals(objectInfo.getObject())
			|| operator == BinaryOperator.LOGICAL_OR	&& Boolean.TRUE.equals(objectInfo.getObject());
	}

	private ParserToolbox createToolboxWithoutEvaluation() {
		return new ParserToolbox(parserToolbox.getThisInfo(), parserToolbox.getSettings(), ParseMode.WITHOUT_EVALUATION);
	}

	@FunctionalInterface
	private interface OperatorImplementation
	{
		ObjectInfo apply(OperatorResultProvider operatorResultProvider, ObjectInfo lhs, ObjectInfo rhs) throws OperatorException;
	}
}
