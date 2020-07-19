package dd.kms.zenodot.utils.dataproviders;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.debug.ParserLogger;
import dd.kms.zenodot.debug.ParserLoggers;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.matching.TypeMatch;
import dd.kms.zenodot.parsers.expectations.ObjectParseResultExpectation;
import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.codecompletions.CodeCompletionFactory;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

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

	private TypeMatch rateFieldByTypes(FieldInfo fieldInfo, Object contextObject, ObjectParseResultExpectation expectation) {
		ObjectInfo fieldValueInfo = parserToolbox.getObjectInfoProvider().getFieldValueInfo(contextObject, fieldInfo);
		TypeInfo type = parserToolbox.getObjectInfoProvider().getType(fieldValueInfo);
		return expectation.rateTypeMatch(type);
	}

	private boolean isFieldAccessDiscouraged(FieldInfo fieldInfo, boolean contextIsStatic) {
		return fieldInfo.isStatic() && !contextIsStatic;
	}

	private MatchRating rateField(FieldInfo fieldInfo, Object contextObject, boolean contextIsStatic, String expectedName, ObjectParseResultExpectation expectation) {
		return MatchRatings.create(rateFieldByName(fieldInfo, expectedName), rateFieldByTypes(fieldInfo, contextObject, expectation), isFieldAccessDiscouraged(fieldInfo, contextIsStatic));
	}
}
