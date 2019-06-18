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
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Optional;

/**
 * Parses expressions of the form {@code (<class>) <expression>} in the context of {@code this}.
 */
public class CastParser extends AbstractParser<ObjectInfo>
{
	public CastParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseOutcome doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		int position = tokenStream.getPosition();
		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != '(') {
			log(LogLevel.ERROR, "expected '('");
			return ParseOutcomes.createParseError(position, "Expected opening parenthesis '('", ErrorPriority.WRONG_PARSER);
		}
		if (characterToken.isContainsCaret()) {
			log(LogLevel.INFO, "potential cast operator; no completion suggestions available");
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		log(LogLevel.INFO, "parsing class at " + tokenStream);
		ParseOutcome classParseOutcome = ParseUtils.parseClass(tokenStream, parserToolbox);
		ParseOutcomeType parseOutcomeType = classParseOutcome.getOutcomeType();
		log(LogLevel.INFO, "parse outcome: " + parseOutcomeType);

		Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(classParseOutcome, ParseExpectation.CLASS, ErrorPriority.POTENTIALLY_RIGHT_PARSER);
		if (parseOutcomeForPropagation.isPresent()) {
			return parseOutcomeForPropagation.get();
		}
		ClassParseResult parseResult = (ClassParseResult) classParseOutcome;
		int parsedToPosition = parseResult.getPosition();

		TypeInfo targetType = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null || characterToken.getValue().charAt(0) != ')') {
			log(LogLevel.ERROR, "missing ')' at " + tokenStream);
			return ParseOutcomes.createParseError(position, "Expected closing parenthesis ')'", ErrorPriority.RIGHT_PARSER);
		}
		log(LogLevel.SUCCESS, "detected cast operator at " + tokenStream);

		if (characterToken.isContainsCaret()) {
			// nothing we can suggest after ')'
			log(LogLevel.INFO, "no completion suggestions available for position " + tokenStream);
			return CompletionSuggestions.none(tokenStream.getPosition());
		}

		return parseAndCast(tokenStream, targetType);
	}

	private ParseOutcome parseAndCast(TokenStream tokenStream, TypeInfo targetType) {
		log(LogLevel.INFO, "parsing object to cast at " + tokenStream);
		ParseOutcome objectParseOutcome = parserToolbox.getSimpleExpressionParser().parse(tokenStream, thisInfo, ParseExpectation.OBJECT);

		Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(objectParseOutcome, ParseExpectation.OBJECT, ErrorPriority.RIGHT_PARSER);
		if (parseOutcomeForPropagation.isPresent()) {
			return parseOutcomeForPropagation.get();
		}
		ObjectParseResult parseResult = (ObjectParseResult) objectParseOutcome;
		int parsedToPosition = parseResult.getPosition();
		ObjectInfo objectInfo = parseResult.getObjectInfo();
		tokenStream.moveTo(parsedToPosition);

		try {
			ObjectInfo castInfo = parserToolbox.getObjectInfoProvider().getCastInfo(objectInfo, targetType);
			log(LogLevel.SUCCESS, "successfully casted object");
			return isCompile()
					? compile(parseResult, targetType, parsedToPosition, castInfo)
					: ParseOutcomes.createObjectParseResult(parsedToPosition, castInfo);
		} catch (ClassCastException e) {
			log(LogLevel.ERROR, "class cast exception: " + e.getMessage());
			return ParseOutcomes.createParseError(tokenStream.getPosition(), "Cannot cast expression to '" + targetType + "'", ErrorPriority.RIGHT_PARSER, e);
		}
	}

	private ParseOutcome compile(ObjectParseResult objectParseResult, TypeInfo targetType, int position, ObjectInfo castInfo) {
		if (!ParseOutcomes.isCompiledParseResult(objectParseResult)) {
			return objectParseResult;
		}
		CompiledObjectParseResult compiledObjectParseResult = (CompiledObjectParseResult) objectParseResult;
		return new CompiledCastParseResult(compiledObjectParseResult, targetType, position, castInfo);
	}

	private static class CompiledCastParseResult extends AbstractCompiledParseResult
	{
		private final CompiledObjectParseResult	compiledObjectParseResult;
		private final TypeInfo								targetType;

		CompiledCastParseResult(CompiledObjectParseResult compiledObjectParseResult, TypeInfo targetType, int position, ObjectInfo castInfo) {
			super(position, castInfo);
			this.compiledObjectParseResult = compiledObjectParseResult;
			this.targetType = targetType;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			ObjectInfo objectInfo = compiledObjectParseResult.evaluate(thisInfo, contextInfo);
			return OBJECT_INFO_PROVIDER.getCastInfo(objectInfo, targetType);
		}
	}
}
