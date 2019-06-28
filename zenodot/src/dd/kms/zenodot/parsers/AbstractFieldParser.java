package dd.kms.zenodot.parsers;

import com.google.common.collect.Iterables;
import dd.kms.zenodot.common.AccessModifier;
import dd.kms.zenodot.common.FieldScanner;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.EvaluationMode;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataProviders.FieldDataProvider;
import dd.kms.zenodot.utils.wrappers.*;

import java.util.List;

import static dd.kms.zenodot.result.ParseError.ErrorPriority;

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
	ParseOutcome parseNext(TokenStream tokenStream, C context, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();

		if (contextCausesNullPointerException(context)) {
			log(LogLevel.ERROR, "null pointer exception");
			return ParseOutcomes.createParseError(startPosition, "Null pointer exception", ErrorPriority.EVALUATION_EXCEPTION);
		}

		if (tokenStream.isCaretWithinNextWhiteSpaces()) {
			String fieldName;
			int insertionEnd;
			try {
				Token fieldNameToken = tokenStream.readIdentifier();
				fieldName = fieldNameToken.getValue();
				insertionEnd = tokenStream.getPosition();
			} catch (TokenStream.JavaTokenParseException e) {
				fieldName = "";
				insertionEnd = startPosition;
			}
			log(LogLevel.INFO, "suggesting fields for completion...");
			return suggestFields(fieldName, context, expectation, startPosition, insertionEnd);
		}

		Token fieldNameToken;
		try {
			fieldNameToken = tokenStream.readIdentifier();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "missing field name at " + tokenStream);
			return ParseOutcomes.createParseError(startPosition, "Expected an identifier", ErrorPriority.WRONG_PARSER);
		}
		String fieldName = fieldNameToken.getValue();
		int endPosition = tokenStream.getPosition();

		// check for code completion
		if (fieldNameToken.isContainsCaret()) {
			log(LogLevel.SUCCESS, "suggesting fields matching '" + fieldName + "'");
			return suggestFields(fieldName, context, expectation, startPosition, endPosition);
		}

		if (tokenStream.hasMore() && tokenStream.peekCharacter() == '(') {
			log(LogLevel.ERROR, "unexpected '(' at " + tokenStream);
			return ParseOutcomes.createParseError(tokenStream.getPosition() + 1, "Unexpected opening parenthesis '('", ErrorPriority.WRONG_PARSER);
		}

		// no code completion requested => field name must exist
		List<FieldInfo> fieldInfos = getFieldInfos(context, getFieldScanner(fieldName, true));
		if (fieldInfos.isEmpty()) {
			if (getFieldInfos(context, getFieldScanner(fieldName, false)).isEmpty()) {
				log(LogLevel.ERROR, "unknown field '" + fieldName + "'");
				return ParseOutcomes.createParseError(startPosition, "Unknown field '" + fieldName + "'", ErrorPriority.POTENTIALLY_RIGHT_PARSER);
			} else {
				log(LogLevel.ERROR, "field '" + fieldName + "' is not visible");
				return ParseOutcomes.createParseError(startPosition, "Field '" + fieldName + "' is not visible", ErrorPriority.RIGHT_PARSER);
			}
		}
		log(LogLevel.SUCCESS, "detected field '" + fieldName + "'");

		FieldInfo fieldInfo = Iterables.getOnlyElement(fieldInfos);
		Object contextObject = getContextObject(context);
		ObjectInfo matchingFieldInfo = parserToolbox.getObjectInfoProvider().getFieldValueInfo(contextObject, fieldInfo);

		int position = tokenStream.getPosition();
		return isCompile()
				? compile(fieldInfo, position, matchingFieldInfo)
				: ParseOutcomes.createObjectParseResult(position, matchingFieldInfo);
	}

	private CompletionSuggestions suggestFields(String expectedName, C context, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		FieldDataProvider fieldDataProvider = parserToolbox.getFieldDataProvider();
		Object contextObject = getContextObject(context);
		List<FieldInfo> fieldInfos = getFieldInfos(context, getFieldScanner());
		boolean contextIsStatic = isContextStatic();
		return fieldDataProvider.suggestFields(expectedName, contextObject, contextIsStatic, fieldInfos, expectation, insertionBegin, insertionEnd);
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

	private ParseOutcome compile(FieldInfo fieldInfo, int position, ObjectInfo objectInfo) {
		return new CompiledFieldParseResult(fieldInfo, position, objectInfo);
	}

	private class CompiledFieldParseResult extends AbstractCompiledParseResult
	{
		private final FieldInfo	fieldInfo;

		CompiledFieldParseResult(FieldInfo fieldInfo, int position, ObjectInfo objectInfo) {
			super(position, objectInfo);
			this.fieldInfo = fieldInfo;
		}

		@Override
		public ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo contextInfo) throws Exception {
			// TODO: If C == TypeInfo, then contextObject should be null instead
			Object contextObject = contextInfo.getObject();
			return OBJECT_INFO_PROVIDER.getFieldValueInfo(contextObject, fieldInfo);
		}
	}
}
