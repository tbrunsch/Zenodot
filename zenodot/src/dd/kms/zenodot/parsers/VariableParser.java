package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.ParseError;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.settings.ParserSettingsBuilder;
import dd.kms.zenodot.settings.Variable;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static dd.kms.zenodot.result.ParseError.ErrorPriority;

/**
 * Parses expressions of the form {@code <variable>} in the (ignored) context of {@code this}, where
 * {@code <variable>} refers to one of the variables specified by {@link ParserSettingsBuilder#addVariable(Variable)}.
 */
public class VariableParser extends AbstractEntityParser<ObjectInfo>
{
	public VariableParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();

		if (tokenStream.isCaretAtPosition()) {
			int insertionEnd;
			try {
				tokenStream.readIdentifier();
				insertionEnd = tokenStream.getPosition();
			} catch (TokenStream.JavaTokenParseException e) {
				insertionEnd = startPosition;
			}
			log(LogLevel.INFO, "suggesting variables for completion...");
			return parserToolbox.getVariableDataProvider().suggestVariables("", expectation, startPosition, insertionEnd);
		}

		Token variableToken;
		try {
			variableToken = tokenStream.readIdentifier();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "missing variable name at " + tokenStream);
			return new ParseError(startPosition, "Expected a variable name", ErrorPriority.WRONG_PARSER);
		}
		String variableName = variableToken.getValue();
		int endPosition = tokenStream.getPosition();

		// check for code completion
		if (variableToken.isContainsCaret()) {
			log(LogLevel.SUCCESS, "suggesting variables matching '" + variableName + "'");
			return parserToolbox.getVariableDataProvider().suggestVariables(variableName, expectation, startPosition, endPosition);
		}

		if (tokenStream.hasMore() && tokenStream.peekCharacter() == '(') {
			log(LogLevel.ERROR, "unexpected '(' at " + tokenStream);
			return new ParseError(tokenStream.getPosition() + 1, "Unexpected opening parenthesis '('", ErrorPriority.WRONG_PARSER);
		}

		// no code completion requested => variable name must exist
		List<Variable> variables = parserToolbox.getSettings().getVariables().stream().sorted(Comparator.comparing(Variable::getName)).collect(Collectors.toList());
		Optional<Variable> firstVariableMatch = variables.stream().filter(variable -> variable.getName().equals(variableName)).findFirst();
		if (!firstVariableMatch.isPresent()) {
			log(LogLevel.ERROR, "unknown variable '" + variableName + "'");
			return new ParseError(startPosition, "Unknown variable '" + variableName + "'", ErrorPriority.POTENTIALLY_RIGHT_PARSER);
		}
		log(LogLevel.SUCCESS, "detected variable '" + variableName + "'");

		Variable matchingVariable = firstVariableMatch.get();
		ObjectInfo matchingVariableInfo = parserToolbox.getObjectInfoProvider().getVariableInfo(matchingVariable);

		return parserToolbox.getObjectTailParser().parse(tokenStream, matchingVariableInfo, expectation);
	}
}
