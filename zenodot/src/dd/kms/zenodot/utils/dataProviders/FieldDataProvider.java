package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.ParserToolbox;
import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLogEntry;
import dd.kms.zenodot.debug.ParserLoggerIF;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.CompletionSuggestionField;
import dd.kms.zenodot.result.CompletionSuggestionIF;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public class FieldDataProvider
{
	private final ParserToolbox parserToolbox;

	public FieldDataProvider(ParserToolbox parserToolbox) {
		this.parserToolbox = parserToolbox;
	}

	public CompletionSuggestions suggestFields(String expectedName, Object contextObject, List<FieldInfo> fieldInfos, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = ParseUtils.createRatedSuggestions(
			fieldInfos,
			fieldInfo -> new CompletionSuggestionField(fieldInfo, insertionBegin, insertionEnd),
			rateFieldByNameAndTypesFunc(expectedName, contextObject, expectation)
		);
		ParserLoggerIF logger = parserToolbox.getSettings().getLogger();
		for (CompletionSuggestionIF suggestion : ratedSuggestions.keySet()) {
			String suggestionText = suggestion.toString();
			int rating = ratedSuggestions.get(suggestion);
			logger.log(new ParserLogEntry(LogLevel.INFO, "FieldDataProvider", suggestionText + ": " + rating));
		}
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private int rateFieldByName(FieldInfo fieldInfo, String expectedName) {
		return ParseUtils.rateStringMatch(fieldInfo.getName(), expectedName);
	}

	private int rateFieldByTypes(FieldInfo fieldInfo, Object contextObject, ParseExpectation expectation) {
		List<TypeInfo> allowedTypes = expectation.getAllowedTypes();
		if (allowedTypes == null) {
			return ParseUtils.TYPE_MATCH_FULL;
		}
		if (allowedTypes.isEmpty()) {
			return ParseUtils.TYPE_MATCH_NONE;
		}
		ObjectInfo fieldValueInfo = parserToolbox.getObjectInfoProvider().getFieldValueInfo(contextObject, fieldInfo);
		TypeInfo type = parserToolbox.getObjectInfoProvider().getType(fieldValueInfo);
		return allowedTypes.stream().mapToInt(allowedType -> ParseUtils.rateTypeMatch(type, allowedType)).min().getAsInt();
	}

	private ToIntFunction<FieldInfo> rateFieldByNameAndTypesFunc(String expectedName, Object contextObject, ParseExpectation expectation) {
		return fieldInfo -> (ParseUtils.TYPE_MATCH_NONE + 1)*rateFieldByName(fieldInfo, expectedName) + rateFieldByTypes(fieldInfo, contextObject, expectation);
	}

	public static String getFieldDisplayText(FieldInfo fieldInfo) {
		return fieldInfo.getName() + " (" + fieldInfo.getDeclaringType() + ")";
	}
}
