package dd.kms.zenodot.parsers;

import dd.kms.zenodot.debug.LogLevel;
import dd.kms.zenodot.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.flowcontrol.EvaluationException;
import dd.kms.zenodot.flowcontrol.InternalErrorException;
import dd.kms.zenodot.flowcontrol.SyntaxException;
import dd.kms.zenodot.parsers.expectations.PackageParseResultExpectation;
import dd.kms.zenodot.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.result.CodeCompletions;
import dd.kms.zenodot.result.PackageParseResult;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.result.ParseResults;
import dd.kms.zenodot.tokenizer.CompletionInfo;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.dataproviders.ClassDataProvider;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for {@link RootpackageParser} and {@link SubpackageParser}
 */
abstract class AbstractPackageParser<C, T extends ParseResult, S extends ParseResultExpectation<T>> extends AbstractParser<C, T, S>
{
	AbstractPackageParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	abstract String getPackagePrefix(C context);

	@Override
	ParseResult doParse(TokenStream tokenStream, C context, S expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		log(LogLevel.INFO, "parsing package");
		PackageParseResult packageParseResult = readPackage(tokenStream, context);

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		char nextChar = tokenStream.readCharacter('.', TokenStream.EMPTY_CHARACTER);
		if (nextChar == TokenStream.EMPTY_CHARACTER) {
			return packageParseResult;
		}

		PackageInfo packageInfo = packageParseResult.getPackage();

		List<AbstractParser<PackageInfo, T, S>> parsers = new ArrayList<>();
		if (!(expectation instanceof PackageParseResultExpectation)) {
			parsers.add(parserToolbox.createParser(QualifiedClassParser.class));
		}
		parsers.add(parserToolbox.createParser(SubpackageParser.class));
		// possible ambiguities: class name identical to subpackage in same package => class name wins
		return ParseUtils.parse(tokenStream, packageInfo, expectation, parsers);
	}

	private PackageParseResult readPackage(TokenStream tokenStream, C context) throws SyntaxException, CodeCompletionException {
		String packagePrefix = getPackagePrefix(context);
		String packageName = packagePrefix + tokenStream.readPackage(info -> suggestPackages(info, context));
		ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();
		if (!classDataProvider.packageExists(packageName)) {
			throw new SyntaxException("Unknown package '" + packageName + "'");
		}
		return ParseResults.createPackageParseResult(InfoProvider.createPackageInfo(packageName));
	}

	private CodeCompletions suggestPackages(CompletionInfo info, C context) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getPackagePrefix(context) + getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting packages matching '" + nameToComplete + "'");

		ClassDataProvider classDataProvider = parserToolbox.getClassDataProvider();
		return classDataProvider.completePackage(insertionBegin, insertionEnd, nameToComplete);
	}
}
