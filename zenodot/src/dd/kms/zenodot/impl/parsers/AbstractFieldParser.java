package dd.kms.zenodot.impl.parsers;

import com.google.common.collect.Iterables;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.common.*;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
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
import dd.kms.zenodot.framework.wrappers.FieldInfo;
import dd.kms.zenodot.framework.wrappers.InfoProvider;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.utils.dataproviders.FieldDataProvider;

import java.util.List;

/**
 * Base class for {@link ClassFieldParser} and {@link ObjectFieldParser}
 */
abstract class AbstractFieldParser<C> extends AbstractParserWithObjectTail<C>
{
	AbstractFieldParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	abstract Object getContextObject(C context);
	abstract Class<?> getContextType(C context);
	abstract boolean isContextStatic();

	@Override
	protected ObjectParseResult parseNext(TokenStream tokenStream, C context, ObjectParseResultExpectation expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		String fieldName = tokenStream.readIdentifier(info -> suggestFields(context, expectation, info), "Expected a field");

		if (tokenStream.peekCharacter() == '(') {
			throw new SyntaxException("Unexpected opening parenthesis '('");
		}
		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		List<FieldInfo> fieldInfos = getFieldInfos(context, getFieldScanner(fieldName, true));
		if (fieldInfos.isEmpty()) {
			throw createFieldNotFoundException(context, fieldName);
		}
		increaseConfidence(ParserConfidence.RIGHT_PARSER);
		log(LogLevel.SUCCESS, "detected field '" + fieldName + "'");

		FieldInfo fieldInfo = Iterables.getOnlyElement(fieldInfos);
		Object contextObject = getContextObject(context);

		ObjectInfo matchingFieldInfo;
		try {
			matchingFieldInfo = parserToolbox.inject(ObjectInfoProvider.class).getFieldValueInfo(contextObject, fieldInfo);
		} catch (AccessDeniedException e) {
			throw new EvaluationException(e.getMessage(), e);
		}

		return new FieldParseResult(isContextStatic(), fieldInfo, matchingFieldInfo, tokenStream);
	}

	private CodeCompletions suggestFields(C context, ObjectParseResultExpectation expectation, CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting fields matching '" + nameToComplete + "'");

		FieldDataProvider fieldDataProvider = parserToolbox.inject(FieldDataProvider.class);
		Object contextObject = getContextObject(context);
		List<FieldInfo> fieldInfos = getFieldInfos(context, getFieldScanner());
		boolean contextIsStatic = isContextStatic();
		return fieldDataProvider.completeField(nameToComplete, contextObject, contextIsStatic, fieldInfos, expectation, insertionBegin, insertionEnd);
	}

	private SyntaxException createFieldNotFoundException(C context, String fieldName) {
		// distinguish between "unknown field" and "field not visible"
		List<FieldInfo> allFields = getFieldInfos(context, getFieldScanner(fieldName, false));
		String error = allFields.isEmpty() ? "Unknown field '"+ fieldName + "'" : "Field '" + fieldName + "' is not visible";
		return new SyntaxException(error);
	}

	private FieldScannerBuilder getFieldScannerBuilder() {
		StaticMode staticMode = isContextStatic() ? StaticMode.STATIC : StaticMode.BOTH;
		AccessModifier minimumAccessModifier = parserToolbox.getSettings().getMinimumFieldAccessModifier();
		return FieldScannerBuilder.create()
			.staticMode(staticMode)
			.minimumAccessModifier(minimumAccessModifier);
	}

	private FieldScanner getFieldScanner() {
		return getFieldScannerBuilder().build();
	}

	private FieldScanner getFieldScanner(String name, boolean considerMinimumAccessModifier) {
		FieldScannerBuilder builder = getFieldScannerBuilder().name(name);
		if (!considerMinimumAccessModifier) {
			builder.minimumAccessModifier(AccessModifier.PRIVATE);
		}
		return builder.build();
	}

	private List<FieldInfo> getFieldInfos(C context, FieldScanner fieldScanner) {
		return InfoProvider.getFieldInfos(getContextType(context), fieldScanner);
	}

	private static class FieldParseResult extends ObjectParseResult
	{
		private final FieldInfo	fieldInfo;
		private final boolean	contextStatic;

		FieldParseResult(boolean contextStatic, FieldInfo fieldInfo, ObjectInfo objectInfo, TokenStream tokenStream) {
			super(objectInfo, tokenStream);
			this.fieldInfo = fieldInfo;
			this.contextStatic = contextStatic;
		}

		@Override
		protected ObjectInfo doEvaluate(ObjectInfo thisInfo, ObjectInfo contextInfo, Variables variables) throws AccessDeniedException {
			Object contextObject = contextStatic ? null : contextInfo.getObject();
			return ObjectInfoProvider.DYNAMIC_OBJECT_INFO_PROVIDER.getFieldValueInfo(contextObject, fieldInfo);
		}
	}
}
