package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataProviders.FieldDataProvider;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.List;
import java.util.Optional;

import static dd.kms.zenodot.result.ParseError.ErrorPriority;

/**
 * Base class for {@link ClassFieldParser} and {@link ObjectFieldParser}
 */
abstract class AbstractFieldParser<C> extends AbstractEntityParser<C>
{
	AbstractFieldParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	abstract boolean contextCausesNullPointerException(C context);
	abstract Object getContextObject(C context);
	abstract boolean isContextStatic();
	abstract List<FieldInfo> getFieldInfos(C context);

	@Override
	ParseResult doParse(TokenStream tokenStream, C context, ParseExpectation expectation) {
		int startPosition = tokenStream.getPosition();

		if (contextCausesNullPointerException(context)) {
			log(LogLevel.ERROR, "null pointer exception");
			return ParseResults.createParseError(startPosition, "Null pointer exception", ErrorPriority.EVALUATION_EXCEPTION);
		}

		if (tokenStream.isCaretAtPosition()) {
			int insertionEnd;
			try {
				tokenStream.readIdentifier();
				insertionEnd = tokenStream.getPosition();
			} catch (TokenStream.JavaTokenParseException e) {
				insertionEnd = startPosition;
			}
			log(LogLevel.INFO, "suggesting fields for completion...");
			return suggestFields("", context, expectation, startPosition, insertionEnd);
		}

		Token fieldNameToken;
		try {
			fieldNameToken = tokenStream.readIdentifier();
		} catch (TokenStream.JavaTokenParseException e) {
			log(LogLevel.ERROR, "missing field name at " + tokenStream);
			return ParseResults.createParseError(startPosition, "Expected an identifier", ErrorPriority.WRONG_PARSER);
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
			return ParseResults.createParseError(tokenStream.getPosition() + 1, "Unexpected opening parenthesis '('", ErrorPriority.WRONG_PARSER);
		}

		// no code completion requested => field name must exist
		List<FieldInfo> fieldInfos = getFieldInfos(context);
		Optional<FieldInfo> firstFieldInfoMatch = fieldInfos.stream().filter(fieldInfo -> fieldInfo.getName().equals(fieldName)).findFirst();
		if (!firstFieldInfoMatch.isPresent()) {
			log(LogLevel.ERROR, "unknown field '" + fieldName + "'");
			return ParseResults.createParseError(startPosition, "Unknown field '" + fieldName + "'", ErrorPriority.POTENTIALLY_RIGHT_PARSER);
		}
		log(LogLevel.SUCCESS, "detected field '" + fieldName + "'");

		FieldInfo fieldInfo = firstFieldInfoMatch.get();
		Object contextObject = getContextObject(context);
		ObjectInfo matchingFieldInfo = parserToolbox.getObjectInfoProvider().getFieldValueInfo(contextObject, fieldInfo);

		return parserToolbox.getObjectTailParser().parse(tokenStream, matchingFieldInfo, expectation);
	}

	private CompletionSuggestions suggestFields(String expectedName, C context, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		FieldDataProvider fieldDataProvider = parserToolbox.getFieldDataProvider();
		Object contextObject = getContextObject(context);
		List<FieldInfo> fieldInfos = getFieldInfos(context);
		boolean contextIsStatic = isContextStatic();
		return fieldDataProvider.suggestFields(expectedName, contextObject, contextIsStatic, fieldInfos, expectation, insertionBegin, insertionEnd);
	}
}
