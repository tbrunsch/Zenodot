package dd.kms.zenodot.utils.dataProviders;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.debug.ParserLoggers;
import dd.kms.zenodot.matching.*;
import dd.kms.zenodot.parsers.ParseExpectation;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionFactory;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for providing information about fields
 */
public class FieldDataProvider
{
	private final ParserToolbox parserToolbox;

	public FieldDataProvider(ParserToolbox parserToolbox) {
		this.parserToolbox = parserToolbox;
	}

	public CompletionSuggestions suggestFields(String expectedName, Object contextObject, boolean contextIsStatic, List<FieldInfo> fieldInfos, ParseExpectation expectation, int insertionBegin, int insertionEnd) {
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
			fieldInfos,
			fieldInfo -> CompletionSuggestionFactory.fieldSuggestion(fieldInfo, insertionBegin, insertionEnd),
			rateFieldFunc(contextObject, contextIsStatic, expectedName, expectation)
		);
		ParserLogger logger = parserToolbox.getSettings().getLogger();
		for (CompletionSuggestion suggestion : ratedSuggestions.keySet()) {
			String suggestionText = suggestion.toString();
			MatchRating rating = ratedSuggestions.get(suggestion);
			logger.log(ParserLoggers.createLogEntry(LogLevel.INFO, "FieldDataProvider", suggestionText + ": " + rating));
		}
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private StringMatch rateFieldByName(FieldInfo fieldInfo, String expectedName) {
		return MatchRatings.rateStringMatch(fieldInfo.getName(), expectedName);
	}

	private TypeMatch rateFieldByTypes(FieldInfo fieldInfo, Object contextObject, ParseExpectation expectation) {
		List<TypeInfo> allowedTypes = expectation.getAllowedTypes();
		if (allowedTypes == null) {
			return TypeMatch.FULL;
		}
		if (allowedTypes.isEmpty()) {
			return TypeMatch.NONE;
		}
		ObjectInfo fieldValueInfo = parserToolbox.getObjectInfoProvider().getFieldValueInfo(contextObject, fieldInfo);
		TypeInfo type = parserToolbox.getObjectInfoProvider().getType(fieldValueInfo);
		return allowedTypes.stream().map(allowedType -> MatchRatings.rateTypeMatch(type, allowedType)).min(TypeMatch::compareTo).get();
	}

	private AccessMatch rateFieldByAccess(FieldInfo fieldInfo, boolean contextIsStatic) {
		return fieldInfo.isStatic() && !contextIsStatic ? AccessMatch.STATIC_ACCESS_VIA_INSTANCE : AccessMatch.FULL;
	}

	private Function<FieldInfo, MatchRating> rateFieldFunc(Object contextObject, boolean contextIsStatic, String expectedName, ParseExpectation expectation) {
		return fieldInfo -> MatchRatings.create(rateFieldByName(fieldInfo, expectedName), rateFieldByTypes(fieldInfo, contextObject, expectation), rateFieldByAccess(fieldInfo, contextIsStatic));
	}
}
