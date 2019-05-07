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
 * Parses subexpressions of the form {@code <package>.<class name>} or {@code <class name>} in
 * the (ignored) context of {@code this}. The unqualified form can only be parsed if either the
 * class or its package is imported (see {@link ParserSettingsBuilder#importPackages(Set)}
 * and {@link ParserSettingsBuilder#importClasses(Set)}).
 */
public class ClassParser extends AbstractEntityParser<ObjectInfo>
{
	public ClassParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResult doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
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

		String packageOrClassName = "";
		int identifierStartPosition;
		while (true) {
			identifierStartPosition = tokenStream.getPosition();
			Token packageOrClassToken;
			try {
				packageOrClassToken = tokenStream.readPackageOrClass();
			} catch (TokenStream.JavaTokenParseException e) {
				return ParseResults.createParseError(identifierStartPosition, "Expected sub-package or class name", ErrorPriority.WRONG_PARSER);
			}
			packageOrClassName += packageOrClassToken.getValue();
			if (packageOrClassToken.isContainsCaret()) {
				return classDataProvider.suggestClassesAndPackages(identifierStartPosition, tokenStream.getPosition(), packageOrClassName);
			}

			Class<?> detectedClass = classDataProvider.detectClass(packageOrClassName);
			if (detectedClass != null) {
				return ParseResults.createClassParseResult(tokenStream.getPosition(), InfoProvider.createTypeInfo(detectedClass));
			}

			Token characterToken = tokenStream.readCharacterUnchecked();
			if (characterToken == null ||  characterToken.getValue().charAt(0) != '.') {
				return ParseResults.createParseError(identifierStartPosition, "Unknown class name '" + packageOrClassName + "'", ErrorPriority.POTENTIALLY_RIGHT_PARSER);
			}

			if (!classDataProvider.packageExists(packageOrClassName)) {
				return ParseResults.createParseError(tokenStream.getPosition(), "Unknown class or package '" + packageOrClassName + "'", ErrorPriority.POTENTIALLY_RIGHT_PARSER);
			}

			packageOrClassName += ".";
			if (characterToken.isContainsCaret()) {
				return classDataProvider.suggestClassesAndPackages(tokenStream.getPosition(), tokenStream.getPosition(), packageOrClassName);
			}
		}
	}
}
