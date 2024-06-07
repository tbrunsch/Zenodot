package dd.kms.zenodot.impl.utils.dataproviders;

import dd.kms.zenodot.api.common.AccessDeniedException;
import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.debug.ParserLogger;
import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.framework.common.ObjectInfoProvider;
import dd.kms.zenodot.framework.matching.MatchRatings;
import dd.kms.zenodot.framework.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.framework.result.CodeCompletions;
import dd.kms.zenodot.framework.utils.ParseUtils;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.FieldInfo;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.debug.ParserLoggers;
import dd.kms.zenodot.impl.result.codecompletions.CodeCompletionFactory;

import java.util.List;

/**
 * Utility class for providing information about fields
 */
public class FieldDataProvider
{
	private final ParserToolbox parserToolbox;

	public FieldDataProvider(ParserToolbox parserToolbox) {
		this.parserToolbox = parserToolbox;
	}

	public CodeCompletions completeField(String expectedName, Object contextObject, boolean contextIsStatic, List<FieldInfo> fieldInfos, ObjectParseResultExpectation expectation, int insertionBegin, int insertionEnd) {
		List<CodeCompletion> codeCompletions = ParseUtils.createCodeCompletions(
			fieldInfos,
			fieldInfo -> CodeCompletionFactory.fieldCompletion(fieldInfo, insertionBegin, insertionEnd, rateField(fieldInfo, contextObject, contextIsStatic, expectedName, expectation))
		);
		ParserLogger logger = parserToolbox.getSettings().getLogger();
		for (CodeCompletion codeCompletion : codeCompletions) {
			String completionText = codeCompletion.toString();
			MatchRating rating = codeCompletion.getRating();
			logger.log(ParserLoggers.createLogEntry(LogLevel.INFO, "FieldDataProvider", completionText + ": " + rating));
		}
		return new CodeCompletions(codeCompletions);
	}

	private StringMatch rateFieldByName(FieldInfo fieldInfo, String expectedName) {
		return MatchRatings.rateStringMatch(expectedName, fieldInfo.getName());
	}

	private TypeMatch rateFieldByTypes(FieldInfo fieldInfo, Object contextObject, ObjectParseResultExpectation expectation) throws AccessDeniedException {
		ObjectInfo fieldValueInfo = parserToolbox.inject(ObjectInfoProvider.class).getFieldValueInfo(contextObject, fieldInfo);
		Class<?> type = parserToolbox.inject(ObjectInfoProvider.class).getType(fieldValueInfo);
		return expectation.rateTypeMatch(type);
	}

	private boolean isFieldAccessDiscouraged(FieldInfo fieldInfo, boolean contextIsStatic) {
		return fieldInfo.isStatic() && !contextIsStatic;
	}

	private MatchRating rateField(FieldInfo fieldInfo, Object contextObject, boolean contextIsStatic, String expectedName, ObjectParseResultExpectation expectation) throws AccessDeniedException {
		return MatchRatings.create(rateFieldByName(fieldInfo, expectedName), rateFieldByTypes(fieldInfo, contextObject, expectation), isFieldAccessDiscouraged(fieldInfo, contextIsStatic));
	}
}
