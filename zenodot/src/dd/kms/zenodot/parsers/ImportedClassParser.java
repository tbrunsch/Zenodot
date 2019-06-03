package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.ClassParseResult;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.result.ParseResults;
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
import java.util.Set;

import static dd.kms.zenodot.result.ParseError.ErrorPriority;

/**
 * Parses subexpressions of the form {@code <class name>} in the context of {@code this} (ignored).
 * The parsing will only be successful if either the class or its package is imported
 * (see {@link ParserSettingsBuilder#importPackages(Iterable)} and {@link ParserSettingsBuilder#importClasses(Iterable)}).
 */
public class ImportedClassParser extends AbstractEntityParser<ObjectInfo>
{
	public ImportedClassParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
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
		ParseResult classParseResult = readClass(tokenStream);
		log(LogLevel.INFO, "parse result: " + classParseResult.getResultType());

		Optional<ParseResult> parseResultForPropagation = ParseUtils.prepareParseResultForPropagation(classParseResult, ParseExpectation.CLASS, ErrorPriority.WRONG_PARSER);
		if (parseResultForPropagation.isPresent()) {
			return parseResultForPropagation.get();
		}

		ClassParseResult parseResult = (ClassParseResult) classParseResult;
		int parsedToPosition = parseResult.getPosition();
		TypeInfo type = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		return parserToolbox.getClassTailParser().parse(tokenStream, type, expectation);
	}

	private ParseResult readClass(TokenStream tokenStream) {
		ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();

		int identifierStartPosition = tokenStream.getPosition();
		Token classToken;
		try {
			classToken = tokenStream.readClass();
		} catch (TokenStream.JavaTokenParseException e) {
			return ParseResults.createParseError(identifierStartPosition, "Expected class name", ErrorPriority.WRONG_PARSER);
		}
		String className = classToken.getValue();
		if (classToken.isContainsCaret()) {
			return classDataProvider.suggestImportedClasses(identifierStartPosition, tokenStream.getPosition(), className);
		}

		Class<?> importedClass = classDataProvider.getImportedClass(className);
		if (importedClass != null) {
			return ParseResults.createClassParseResult(tokenStream.getPosition(), InfoProvider.createTypeInfo(importedClass));
		}
		return ParseResults.createParseError(tokenStream.getPosition(), "Unknown class '" + className + "'", ErrorPriority.POTENTIALLY_RIGHT_PARSER);
	}
}
