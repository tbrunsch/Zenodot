package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.result.*;
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
abstract class AbstractPackageParser<C> extends AbstractParser<C>
{
	AbstractPackageParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	abstract String getPackagePrefix(C contextInfo);

	@Override
	ParseOutcome doParse(TokenStream tokenStream, C contextInfo, ParseExpectation expectation) {
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
		ParseOutcome packageParseOutcome = readPackage(tokenStream, contextInfo);
		log(LogLevel.INFO, "parse outcome: " + packageParseOutcome.getOutcomeType());

		Optional<ParseOutcome> parseOutcomeForPropagation = ParseUtils.prepareParseOutcomeForPropagation(packageParseOutcome, ParseExpectation.PACKAGE, ErrorPriority.WRONG_PARSER);
		if (parseOutcomeForPropagation.isPresent()) {
			return parseOutcomeForPropagation.get();
		}

		PackageParseResult parseResult = (PackageParseResult) packageParseOutcome;
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
			return ParseOutcomes.createParseError(parsedToPosition, "Unexpected character '" + nextChar + "'", ErrorPriority.RIGHT_PARSER);
		}
		log(LogLevel.INFO, "detected '.'");

		AbstractParser<PackageInfo>[] parsers = expectation.getResultType() == ParseResultType.PACKAGE
														? new AbstractParser[]{ parserToolbox.getSubpackageParser() }
														: new AbstractParser[] { parserToolbox.getQualifiedClassParser(), parserToolbox.getSubpackageParser() };
		return ParseUtils.parse(tokenStream, packageInfo, expectation,	parsers);
	}

	private ParseOutcome readPackage(TokenStream tokenStream, C contextInfo) {
		ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();
		String packageName = getPackagePrefix(contextInfo);
		int identifierStartPosition = tokenStream.getPosition();
		Token packageToken;
		try {
			packageToken = tokenStream.readPackage();
		} catch (TokenStream.JavaTokenParseException e) {
			return ParseOutcomes.createParseError(identifierStartPosition, "Expected package name", ErrorPriority.WRONG_PARSER);
		}
		packageName += packageToken.getValue();

		if (packageToken.isContainsCaret()) {
			return classDataProvider.suggestPackages(identifierStartPosition, tokenStream.getPosition(), packageName);
		}
		if (!classDataProvider.packageExists(packageName)) {
			return ParseOutcomes.createParseError(tokenStream.getPosition(), "Unknown package '" + packageName + "'", ErrorPriority.WRONG_PARSER);
		}
		return ParseOutcomes.createPackageParseResult(tokenStream.getPosition(), InfoProvider.createPackageInfo(packageName));
	}
}
