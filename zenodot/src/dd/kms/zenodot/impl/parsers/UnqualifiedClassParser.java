package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.framework.result.CodeCompletions;
import dd.kms.zenodot.framework.result.ParseResult;
import dd.kms.zenodot.framework.tokenizer.CompletionInfo;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.utils.dataproviders.ClassDataProvider;

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
	protected ParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, S expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		String className = tokenStream.readClass(this::suggestClasses);

		ClassDataProvider classDataProvider = parserToolbox.inject(ClassDataProvider.class);
		Class<?> importedClass = classDataProvider.getImportedClass(className);

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		if (importedClass == null) {
			throw new SyntaxException("Unknown class '" + className + "'");
		}

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		return parserToolbox.createParser(ClassTailParser.class).parse(tokenStream, importedClass, expectation);
	}

	private CodeCompletions suggestClasses(CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting classes matching '" + nameToComplete + "'");

		ClassDataProvider classDataProvider = parserToolbox.inject(ClassDataProvider.class);
		return classDataProvider.completeClassName(insertionBegin, insertionEnd, nameToComplete, considerAllClassesForCompletions);
	}
}
