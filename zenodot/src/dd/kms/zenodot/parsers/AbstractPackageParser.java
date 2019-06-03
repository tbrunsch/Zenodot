package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.PackageParseResult;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.result.ParseResultType;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.tokenizer.Token;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataProviders.ClassDataProvider;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

import java.util.Optional;

import static dd.kms.zenodot.result.ParseError.ErrorPriority;

/**
 * Base class for {@link RootpackageParser} and {@link SubpackageParser}
 */
abstract class AbstractPackageParser<C> extends AbstractEntityParser<C>
{
	AbstractPackageParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	abstract String getPackagePrefix(C contextInfo);

	@Override
	ParseResult doParse(TokenStream tokenStream, C contextInfo, ParseExpectation expectation) {
		if (tokenStream.isCaretWithinNextWhiteSpaces()) {
			int insertionBegin = tokenStream.getPosition();
			String packageName = getPackagePrefix(contextInfo);
			int insertionEnd;
			try {
				Token packageNameToken = tokenStream.readPackage();
				packageName += packageNameToken.getValue();
				insertionEnd = tokenStream.getPosition();
			} catch (TokenStream.JavaTokenParseException e) {
				insertionEnd = insertionBegin;
			}
			log(LogLevel.INFO, "suggesting packages for completion...");
			ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();
			return classDataProvider.suggestPackages(insertionBegin, insertionEnd, packageName);
		}

		log(LogLevel.INFO, "parsing package");
		ParseResult packageParseResult = readPackage(tokenStream, contextInfo);
		log(LogLevel.INFO, "parse result: " + packageParseResult.getResultType());

		Optional<ParseResult> parseResultForPropagation = ParseUtils.prepareParseResultForPropagation(packageParseResult, ParseExpectation.PACKAGE, ErrorPriority.WRONG_PARSER);
		if (parseResultForPropagation.isPresent()) {
			return parseResultForPropagation.get();
		}

		PackageParseResult parseResult = (PackageParseResult) packageParseResult;
		int parsedToPosition = parseResult.getPosition();
		PackageInfo packageInfo = parseResult.getPackage();

		tokenStream.moveTo(parsedToPosition);

		Token characterToken = tokenStream.readCharacterUnchecked();
		if (characterToken == null) {
			return parseResult;
		}
		char nextChar = characterToken.getValue().charAt(0);
		if (nextChar != '.') {
			log(LogLevel.ERROR, "detected '" + nextChar + "'");
			return ParseResults.createParseError(parsedToPosition, "Unexpected character '" + nextChar + "'", ErrorPriority.RIGHT_PARSER);
		}
		log(LogLevel.INFO, "detected '.'");

		AbstractEntityParser<PackageInfo>[] parsers = expectation.getEvaluationType() == ParseResultType.PACKAGE_PARSE_RESULT
														? new AbstractEntityParser[]{ parserToolbox.getSubpackageParser() }
														: new AbstractEntityParser[] { parserToolbox.getQualifiedClassParser(), parserToolbox.getSubpackageParser() };
		return ParseUtils.parse(tokenStream, packageInfo, expectation,	parsers);
	}

	private ParseResult readPackage(TokenStream tokenStream, C contextInfo) {
		ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();
		String packageName = getPackagePrefix(contextInfo);
		int identifierStartPosition = tokenStream.getPosition();
		Token packageToken;
		try {
			packageToken = tokenStream.readPackage();
		} catch (TokenStream.JavaTokenParseException e) {
			return ParseResults.createParseError(identifierStartPosition, "Expected package name", ErrorPriority.WRONG_PARSER);
		}
		packageName += packageToken.getValue();

		if (packageToken.isContainsCaret()) {
			return classDataProvider.suggestPackages(identifierStartPosition, tokenStream.getPosition(), packageName);
		}
		if (!classDataProvider.packageExists(packageName)) {
			return ParseResults.createParseError(tokenStream.getPosition(), "Unknown package '" + packageName + "'", ErrorPriority.WRONG_PARSER);
		}
		return ParseResults.createPackageParseResult(tokenStream.getPosition(), InfoProvider.createPackageInfo(packageName));
	}
}
