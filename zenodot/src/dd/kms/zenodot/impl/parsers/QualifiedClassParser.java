package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.impl.flowcontrol.CodeCompletionException;
import dd.kms.zenodot.impl.flowcontrol.EvaluationException;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.api.result.ParseResult;
import dd.kms.zenodot.impl.tokenizer.CompletionInfo;
import dd.kms.zenodot.impl.tokenizer.TokenStream;
import dd.kms.zenodot.impl.utils.ClassUtils;
import dd.kms.zenodot.impl.utils.ParserToolbox;
import dd.kms.zenodot.impl.utils.dataproviders.ClassDataProvider;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.PackageInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

/**
 * Parses subexpressions {@code <class name>} of expressions of the form {@code <package name>.<class name>}.
 * The package {@code <package name>} is the context for the parser.
 */
public class QualifiedClassParser<T extends ParseResult, S extends ParseResultExpectation<T>> extends AbstractParser<PackageInfo, T, S>
{
	public QualifiedClassParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	ParseResult doParse(TokenStream tokenStream, PackageInfo contextInfo, S expectation) throws SyntaxException, CodeCompletionException, InternalErrorException, EvaluationException {
		String className = tokenStream.readIdentifier(info -> suggestQualifiedClasses(contextInfo, expectation, info), "Expected a class");

		increaseConfidence(ParserConfidence.POTENTIALLY_RIGHT_PARSER);

		String qualifiedClassName = contextInfo.getPackageName() + "." + className;
		Class<?> clazz = ClassUtils.getClassUnchecked(qualifiedClassName);
		if (clazz == null) {
			throw new SyntaxException("Unknown class '" + qualifiedClassName + "'");
		}
		log(LogLevel.SUCCESS, "detected class '" + qualifiedClassName + "'");
		increaseConfidence(ParserConfidence.RIGHT_PARSER);

		TypeInfo typeInfo = InfoProvider.createTypeInfo(clazz);
		return parserToolbox.createParser(ClassTailParser.class).parse(tokenStream, typeInfo, expectation);
	}

	private CodeCompletions suggestQualifiedClasses(PackageInfo contextInfo, S expectation, CompletionInfo info) {
		int insertionBegin = getInsertionBegin(info);
		int insertionEnd = getInsertionEnd(info);
		String nameToComplete = getTextToComplete(info);

		log(LogLevel.SUCCESS, "suggesting classes matching '" + nameToComplete + "'");

		String classPrefixWithPackage = contextInfo.getPackageName() + "." + nameToComplete;
		return ClassDataProvider.completeQualifiedClasses(insertionBegin, insertionEnd, classPrefixWithPackage);
	}
}
