package dd.kms.zenodot.parsers;

import com.google.common.collect.Iterables;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.common.FieldScanner;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.flowcontrol.InternalErrorException;
import dd.kms.zenodot.flowcontrol.InternalEvaluationException;
import dd.kms.zenodot.flowcontrol.InternalParseException;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.AbstractCompiledParseResult;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.tokenizer.CompletionInfo;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataproviders.FieldDataProvider;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;

/**
 * Base class for {@link ClassFieldParser} and {@link ObjectFieldParser}
 */
abstract class AbstractFieldParser<C> extends AbstractParserWithObjectTail<C>
{
	AbstractFieldParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	abstract boolean contextCausesNullPointerException(C context);
	abstract Object getContextObject(C context);
	abstract TypeInfo getContextType(C context);
	abstract boolean isContextStatic();

	@Override
	ObjectParseResult parseNext(TokenStream tokenStream, C context, ObjectParseResultExpectation expectation) throws InternalParseException, CodeCompletionException, InternalErrorException, InternalEvaluationException {
		if (contextCausesNullPointerException(context)) {
			throw new InternalParseException("Null pointer exception");
		}

		String fieldName = tokenStream.readIdentifier(info -> suggestFields(context, expectation, info), "Expected a field");

		if (tokenStream.peekCharacter() == '(') {
			throw new InternalParseException("Unexpected opening parenthesis '('");
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
		ObjectInfo matchingFieldInfo = parserToolbox.getObjectInfoProvider().getFieldValueInfo(contextObject, fieldInfo);

		return isCompile()
				? compile(fieldInfo, matchingFieldInfo)
				: ParseResults.createObjectParseResult(matchingFieldInfo);
	}

	private CodeCompletions suggestFields(C context, ObjectParseResultExpectation expectation, CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting fields matching '" + nameToComplete + "'");

		FieldDataProvider fieldDataProvider = parserToolbox.getFieldDataProvider();
		Object contextObject = getContextObject(context);
		List<FieldInfo> fieldInfos = getFieldInfos(context, getFieldScanner());
		boolean contextIsStatic = isContextStatic();
		return fieldDataProvider.completeField(nameToComplete, contextObject, contextIsStatic, fieldInfos, expectation, insertionBegin, insertionEnd);
	}

	private InternalParseException createFieldNotFoundException(C context, String fieldName) {
		// distinguish between "unknown field" and "field not visible"
		List<FieldInfo> allFields = getFieldInfos(context, getFieldScanner(fieldName, false));
		String error = allFields.isEmpty() ? "Unknown field '"+ fieldName + "'" : "Field '" + fieldName + "' is not visible";
		return new InternalParseException(error);
	}

	private FieldScanner getFieldScanner() {
		AccessModifier minimumAccessLevel = parserToolbox.getSettings().getMinimumAccessLevel();
		return new FieldScanner().staticOnly(isContextStatic()).minimumAccessLevel(minimumAccessLevel);
	}

	private FieldScanner getFieldScanner(String name, boolean considerMinimumAccessLevel) {
		FieldScanner fieldScanner = getFieldScanner().name(name);
		if (!considerMinimumAccessLevel) {
			fieldScanner.minimumAccessLevel(AccessModifier.PRIVATE);
		}
		return fieldScanner;
	}

	private List<FieldInfo> getFieldInfos(C context, FieldScanner fieldScanner) {
		return InfoProvider.getFieldInfos(getContextType(context), fieldScanner);
	}

	private ObjectParseResult compile(FieldInfo fieldInfo, ObjectInfo objectInfo) {
		return new CompiledFieldParseResult(fieldInfo, objectInfo);
	}

	private class CompiledFieldParseResult extends AbstractCompiledParseResult
	{
		private final FieldInfo	fieldInfo;

		CompiledFieldParseResult(FieldInfo fieldInfo, ObjectInfo objectInfo) {
			super(objectInfo);
			this.fieldInfo = fieldInfo;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			Object contextObject = isContextStatic() ? null : contextInfo.getObject();
			return OBJECT_INFO_PROVIDER.getFieldValueInfo(contextObject, fieldInfo);
		}
	}
}
