package dd.kms.zenodot.impl.parsers;

import com.google.common.base.Preconditions;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParserWithObjectTail;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.CodeCompletions;
import dd.kms.zenodot.framework.result.ObjectParseResult;
import dd.kms.zenodot.framework.tokenizer.CompletionInfo;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.VariablesImpl;
import dd.kms.zenodot.impl.utils.dataproviders.VariableDataProvider;

/**
 * Parses expressions of the form {@code <variable>} in the (ignored) context of {@code this}, where
 * {@code <variable>} refers to one of the variables of {@code getParserToolBox().getVariables()}.
 */
public class VariableParser extends AbstractParserWithObjectTail<ObjectInfo>
{
	public VariableParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	protected ObjectParseResult parseNext(TokenStream tokenStream, ObjectInfo contextInfo, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException {
		String variableName = tokenStream.readIdentifier(info -> suggestVariables(expectation, info), "Expected a variable");

		if (tokenStream.peekCharacter() == '(') {
			throw new SyntaxException("Unexpected opening parenthesis '('");
		}

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		VariablesImpl variables = (VariablesImpl) parserToolbox.getVariables();
		if (!variables.getNames().contains(variableName)) {
			throw new SyntaxException("Unknown variable '" + variableName + "'");
		}
		ObjectInfo variableValueInfo = variables.getValueInfo(variableName);
		log(LogLevel.SUCCESS, "detected variable '" + variableName + "'");

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		return new VariableParseResult(variableName, variableValueInfo, tokenStream);
	}

	private CodeCompletions suggestVariables(ObjectParseResultExpectation expectation, CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting variables matching '" + nameToComplete + "'");

		VariableDataProvider variableDataProvider = parserToolbox.inject(VariableDataProvider.class);
		return variableDataProvider.completeVariable(nameToComplete, expectation, insertionBegin, insertionEnd);
	}

	private static class VariableParseResult extends ObjectParseResult
	{
		private final String variableName;

		VariableParseResult(String variableName, ObjectInfo variableValueInfo, TokenStream tokenStream) {
			super(variableValueInfo, tokenStream);
			this.variableName = variableName;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo, Variables variables) {
			Preconditions.checkArgument(variables instanceof VariablesImpl);
			return ((VariablesImpl) variables).getValueInfo(variableName);
		}
	}
}
