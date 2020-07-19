package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.flowcontrol.InternalErrorException;
import dd.kms.zenodot.flowcontrol.InternalParseException;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.settings.ParserSettingsBuilder;
import dd.kms.zenodot.settings.Variable;
import dd.kms.zenodot.tokenizer.CompletionInfo;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataproviders.VariableDataProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.List;
import java.util.Optional;

/**
 * Parses expressions of the form {@code <variable>} in the (ignored) context of {@code this}, where
 * {@code <variable>} refers to one of the variables specified by {@link ParserSettingsBuilder#variables(List)}.
 */
public class VariableParser extends AbstractParserWithObjectTail<ObjectInfo>
{
	public VariableParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, InternalErrorException {
		String variableName = tokenStream.readIdentifier(info -> suggestVariables(expectation, info), "Expected a variable");

		if (tokenStream.peekCharacter() == '(') {
			throw new InternalParseException("Unexpected opening parenthesis '('");
		}

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		Optional<Variable> variable = parserToolbox.getSettings().getVariables().stream()
			.filter(v -> v.getName().equals(variableName))
			.findFirst();
		if (!variable.isPresent()) {
			throw new InternalParseException("Unknown variable '" + variableName + "'");
		}
		log(LogLevel.SUCCESS, "detected variable '" + variableName + "'");

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		ObjectInfo variableInfo = variable.get().getValue();

		return isCompile()
				? ParseResults.createCompiledConstantObjectParseResult(variableInfo)
				: ParseResults.createObjectParseResult(variableInfo);
	}

	private CodeCompletions suggestVariables(ObjectParseResultExpectation expectation, CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting variables matching '" + nameToComplete + "'");

		VariableDataProvider variableDataProvider = parserToolbox.getVariableDataProvider();
		return variableDataProvider.completeVariable(nameToComplete, expectation, insertionBegin, insertionEnd);
	}
}
