package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.framework.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.framework.flowcontrol.EvaluationException;
import dd.kms.zenodot.framework.flowcontrol.InternalErrorException;
import dd.kms.zenodot.framework.flowcontrol.SyntaxException;
import dd.kms.zenodot.framework.parsers.AbstractParser;
import dd.kms.zenodot.framework.parsers.ParserConfidence;
import dd.kms.zenodot.framework.parsers.expectations.PackageParseResultExpectation;
import dd.kms.zenodot.framework.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.framework.result.CodeCompletions;
import dd.kms.zenodot.framework.result.PackageParseResult;
import dd.kms.zenodot.framework.result.ParseResult;
import dd.kms.zenodot.framework.result.ParseResults;
import dd.kms.zenodot.framework.tokenizer.CompletionInfo;
import dd.kms.zenodot.framework.tokenizer.TokenStream;
import dd.kms.zenodot.framework.utils.ParseUtils;
import dd.kms.zenodot.framework.utils.ParserToolbox;
import dd.kms.zenodot.impl.utils.dataproviders.ClassDataProvider;

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
	protected ParseResult doParse(TokenStream tokenStream, C context, S expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		log(LogLevel.INFO, "parsing package");
		PackageParseResult packageParseResult = readPackage(tokenStream, context);

		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		char nextChar = tokenStream.readCharacter('.', TokenStream.EMPTY_CHARACTER);
		if (nextChar == TokenStream.EMPTY_CHARACTER) {
			return packageParseResult;
		}

		String packageName = packageParseResult.getPackageName();

		List<AbstractParser<String, T, S>> parsers = new ArrayList<>();
		if (!(expectation instanceof PackageParseResultExpectation)) {
			parsers.add(parserToolbox.createParser(QualifiedClassParser.class));
		}
		parsers.add(parserToolbox.createParser(SubpackageParser.class));
		// possible ambiguities: class name identical to subpackage in same package => class name wins
		return ParseUtils.parse(tokenStream, packageName, expectation, parsers);
	}

	private PackageParseResult readPackage(TokenStream tokenStream, C context) throws SyntaxException, CodeCompletionException {
		String packagePrefix = getPackagePrefix(context);
		String packageName = packagePrefix + tokenStream.readPackage(info -> suggestPackages(info, context));
		if (!ClassDataProvider.packageExists(packageName)) {
			throw new SyntaxException("Unknown package '" + packageName + "'");
		}
		return ParseResults.createPackageParseResult(packageName);
	}

	private CodeCompletions suggestPackages(CompletionInfo info, C context) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getPackagePrefix(context) + getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting packages matching '" + nameToComplete + "'");

		return ClassDataProvider.completePackage(insertionBegin, insertionEnd, nameToComplete);
	}
}
