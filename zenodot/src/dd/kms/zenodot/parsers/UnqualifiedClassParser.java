package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.*;
import dd.kms.zenodot.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.settings.ParserSettingsBuilder;
import dd.kms.zenodot.tokenizer.CompletionInfo;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataproviders.ClassDataProvider;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * Parses subexpressions of the form {@code <class name>} in the context of {@code this} (ignored).
 * The parsing will only be successful if either the class or its package is imported
 * (see {@link ParserSettingsBuilder#importPackages(Iterable)} and {@link ParserSettingsBuilder#importClasses(Iterable)}).
 * However, the parser will also provide code completions for qualified class names if the class name
 * matches, but is not imported.
 */
public class UnqualifiedClassParser<T extends ParseResult, S extends ParseResultExpectation<T>> extends AbstractParser<ObjectInfo, T, S>
{
	/**
	 * If this flag is true, then also classes that are not imported and whose
	 * package is not imported will be considered when creating code completions.
	 * However, since they are not imported they will be suggested with full
	 * qualification.
	 */
	private final boolean considerAllClassesForCompletions;

	public UnqualifiedClassParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
		considerAllClassesForCompletions = parserToolbox.getSettings().isConsiderAllClassesForClassCompletions();
	}

	@Override
	ParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, S expectation) throws InternalParseException, CodeCompletionException, AmbiguousParseResultException, InternalErrorException, InternalEvaluationException {
		String className = tokenStream.readClass(this::suggestClasses);

		ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();
		Class<?> importedClass = classDataProvider.getImportedClass(className);

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		if (importedClass == null) {
			throw new InternalParseException("Unknown class '" + className + "'");
		}

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		TypeInfo type = InfoProvider.createTypeInfo(importedClass);
		return parserToolbox.createParser(ClassTailParser.class).parse(tokenStream, type, expectation);
	}

	private CodeCompletions suggestClasses(CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting classes matching '" + nameToComplete + "'");

		ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();
		return classDataProvider.completeClassName(insertionBegin, insertionEnd, nameToComplete, considerAllClassesForCompletions);
	}
}
