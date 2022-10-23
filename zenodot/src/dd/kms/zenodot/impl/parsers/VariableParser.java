package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.result.ObjectParseResult;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.settings.Variable;
import dd.kms.zenodot.impl.wrappers.InfoProvider;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.impl.result.ParseResults;
import dd.kms.zenodot.impl.tokenizer.CompletionInfo;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ParserToolbox;
import dd.kms.zenodot.impl.utils.dataproviders.VariableDataProvider;

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
	ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException {
		String variableName = tokenStream.readIdentifier(info -> suggestVariables(expectation, info), "Expected a variable");

		if (tokenStream.peekCharacter() == '(') {
			throw new SyntaxException("Unexpected opening parenthesis '('");
		}

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		Optional<Variable> variable = parserToolbox.getSettings().getVariables().stream()
			.filter(v -> v.getName().equals(variableName))
			.findFirst();
		if (!variable.isPresent()) {
			throw new SyntaxException("Unknown variable '" + variableName + "'");
		}
		log(LogLevel.SUCCESS, "detected variable '" + variableName + "'");

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		return ParseResults.createCompiledConstantObjectParseResult(InfoProvider.createObjectInfo(variable.get().getValue()), tokenStream);
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
