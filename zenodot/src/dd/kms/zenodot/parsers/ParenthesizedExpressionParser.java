package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.result.ParseError.ErrorPriority;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.Optional;

/**
 * Parses expressions of the form {@code (<expression>)} in the context of {@code this}.
 */
public class ParenthesizedExpressionParser extends AbstractParserWithObjectTail<ObjectInfo>
{
	public ParenthesizedExpressionParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ParseOutcome parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
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
			log(LogLevel.INFO, "no code completions available at " + tokenStream);
			return CodeCompletions.none(tokenStream.getPosition());
		}
		position = tokenStream.getPosition();
		return isCompile()
				? compile(expressionParseOutcome, position)
				: ParseOutcomes.createObjectParseResult(position, objectInfo);
	}

	private ParseOutcome compile(ParseOutcome expressionParseOutcome, int position) {
		CompiledObjectParseResult compiledExpressionParseResult = (CompiledObjectParseResult) expressionParseOutcome;
		return ParseOutcomes.deriveCompiledObjectParseResult(compiledExpressionParseResult, position);
	}
}
