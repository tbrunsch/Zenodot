package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.ClassParseResult;
import dd.kms.zenodot.result.ParseError;
import dd.kms.zenodot.result.ParseResultIF;
import dd.kms.zenodot.settings.ParserSettingsBuilder;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataProviders.ClassDataProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

public class ClassParser extends AbstractEntityParser<ObjectInfo>
{
	public ClassParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	ParseResultIF doParse(TokenStream tokenStream, ObjectInfo contextInfo, ParseExpectation expectation) {
		log(LogLevel.INFO, "parsing class");
		ParseResultIF classParseResult = readClass(tokenStream);
		log(LogLevel.INFO, "parse result: " + classParseResult.getResultType());

		if (ParseUtils.propagateParseResult(classParseResult, ParseExpectation.CLASS)) {
			return classParseResult;
		}

		ClassParseResult parseResult = (ClassParseResult) classParseResult;
		int parsedToPosition = parseResult.getPosition();
		TypeInfo type = parseResult.getType();

		tokenStream.moveTo(parsedToPosition);

		return parserToolbox.getClassTailParser().parse(tokenStream, type, expectation);
	}

	private ParseResultIF readClass(TokenStream tokenStream) {
		ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();

		String packageOrClassName = "";
		int identifierStartPosition;
		while (true) {
			identifierStartPosition = tokenStream.getPosition();
			Token packageOrClassToken;
			try {
				packageOrClassToken = tokenStream.readPackageOrClass();
			} catch (TokenStream.JavaTokenParseException e) {
				return new ParseError(identifierStartPosition, "Expected sub-package or class name", ParseError.ErrorType.SYNTAX_ERROR);
			}
			packageOrClassName += packageOrClassToken.getValue();
			if (packageOrClassToken.isContainsCaret()) {
				return classDataProvider.suggestClassesAndPackages(identifierStartPosition, tokenStream.getPosition(), packageOrClassName);
			}

			Class<?> detectedClass = classDataProvider.detectClass(packageOrClassName);
			if (detectedClass != null) {
				return new ClassParseResult(tokenStream.getPosition(), TypeInfo.of(detectedClass));
			}

			Token characterToken = tokenStream.readCharacterUnchecked();
			if (characterToken == null ||  characterToken.getValue().charAt(0) != '.') {
				return new ParseError(identifierStartPosition, "Unknown class name '" + packageOrClassName + "'", ParseError.ErrorType.SEMANTIC_ERROR);
			}

			if (!classDataProvider.packageExists(packageOrClassName)) {
				return new ParseError(tokenStream.getPosition(), "Unknown class or package '" + packageOrClassName + "'", ParseError.ErrorType.SEMANTIC_ERROR);
			}

			packageOrClassName += ".";
			if (characterToken.isContainsCaret()) {
				return classDataProvider.suggestClassesAndPackages(tokenStream.getPosition(), tokenStream.getPosition(), packageOrClassName);
			}
		}
	}
}
