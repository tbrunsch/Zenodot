package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.ClassParseResult;
import dd.kms.zenodot.result.ParseOutcome;
import dd.kms.zenodot.result.ParseOutcomes;
import dd.kms.zenodot.settings.ParserSettingsBuilder;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataProviders.ClassDataProvider;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.Optional;

import static dd.kms.zenodot.result.ParseError.ErrorPriority;

/**
 * Parses subexpressions of the form {@code <class name>} in the context of {@code this} (ignored).
 * The parsing will only be successful if either the class or its package is imported
 * (see {@link ParserSettingsBuilder#importPackages(Iterable)} and {@link ParserSettingsBuilder#importClasses(Iterable)}).
 */
public class ImportedClassParser extends AbstractParser<ObjectInfo>
{
	public ImportedClassParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ParseOutcome doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		if (tokenStream.isCaretWithinNextWhiteSpaces()) {
			int insertionBegin = tokenStream.getPosition();
			String className;
			int insertionEnd;
			try {
				Token classToken = tokenStream.readClass();
				className = classToken.getValue();
				insertionEnd = tokenStream.getPosition();
			} catch (TokenStream.JavaTokenParseException e) {
				className = "";
				insertionEnd = insertionBegin;
			}
			log(LogLevel.INFO, "suggesting imported classes for completion...");
			ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();
			return classDataProvider.suggestImportedClasses(insertionBegin, insertionEnd, className);
		}

		log(LogLevel.INFO, "parsing class");
		ParseOutcome classParseOutcome = readClass(tokenStream);
		log(LogLevel.INFO, "parse outcome: " + classParseOutcome.getOutcomeType());

		Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(classParseOutcome, ParseExpectation.CLASS, ErrorPriority.WRONG_PARSER);
		if (parseOutcomeForPropagation.isPresent()) {
			return parseOutcomeForPropagation.get();
		}

		ClassParseResult parseResult = (ClassParseResult) classParseOutcome;
		int parsedToPosition = parseResult.getPosition();
		TypeInfo type = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		return parserToolbox.getClassTailParser().parse(tokenStream, type, expectation);
	}

	private ParseOutcome readClass(TokenStream tokenStream) {
		ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();

		int identifierStartPosition = tokenStream.getPosition();
		Token classToken;
		try {
			classToken = tokenStream.readClass();
		} catch (TokenStream.JavaTokenParseException e) {
			return ParseOutcomes.createParseError(identifierStartPosition, "Expected class name", ErrorPriority.WRONG_PARSER);
		}
		String className = classToken.getValue();
		if (classToken.isContainsCaret()) {
			return classDataProvider.suggestImportedClasses(identifierStartPosition, tokenStream.getPosition(), className);
		}

		Class<?> importedClass = classDataProvider.getImportedClass(className);
		if (importedClass != null) {
			return ParseOutcomes.createClassParseResult(tokenStream.getPosition(), InfoProvider.createTypeInfo(importedClass));
		}
		return ParseOutcomes.createParseError(tokenStream.getPosition(), "Unknown class '" + className + "'", ErrorPriority.POTENTIALLY_RIGHT_PARSER);
	}
}
