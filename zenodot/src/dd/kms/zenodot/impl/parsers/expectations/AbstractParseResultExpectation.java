package dd.kms.zenodot.impl.parsers.expectations;

import dd.kms.zenodot.impl.common.ObjectInfoProvider;
import dd.kms.zenodot.impl.result.ClassParseResult;
import dd.kms.zenodot.impl.result.ObjectParseResult;
import dd.kms.zenodot.impl.result.PackageParseResult;
import dd.kms.zenodot.impl.result.ParseResult;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.tokenizer.TokenStream;

abstract class AbstractParseResultExpectation<T extends ParseResult> implements ParseResultExpectation<T>
{
	private final Class<T>	expectationClazz;
	private final boolean	parseWholeText;

	AbstractParseResultExpectation(Class<T> expectationClazz, boolean parseWholeText) {
		this.expectationClazz = expectationClazz;
		this.parseWholeText = parseWholeText;
	}

	boolean isParseWholeText() {
		return parseWholeText;
	}

	void doCheck(T parseResult, ObjectInfoProvider objectInfoProvider) throws SyntaxException {}

	@Override
	public final T check(TokenStream tokenStream, ParseResult parseResult, ObjectInfoProvider objectInfoProvider) throws InternalErrorException, SyntaxException {
		if (!expectationClazz.isInstance(parseResult)) {
			String error = "Expected " + getDescription(expectationClazz) + ", but obtained " + getDescription(parseResult.getClass());
			throw new SyntaxException(error);
		}
		T castedParseResult = expectationClazz.cast(parseResult);
		doCheck(castedParseResult, objectInfoProvider);

		if (parseWholeText) {
			char c = tokenStream.peekCharacter();
			if (c != TokenStream.EMPTY_CHARACTER) {
				throw new SyntaxException("Unexpected character '" + c + "'");
			}
		}

		return castedParseResult;
	}

	private String getDescription(Class<? extends ParseResult> parseResultClass) throws InternalErrorException {
		if (ObjectParseResult.class.isAssignableFrom(parseResultClass)) {
			return "an object";
		} else if (PackageParseResult.class.isAssignableFrom(parseResultClass)) {
			return "a package";
		} else if (ClassParseResult.class.isAssignableFrom(parseResultClass)) {
			return "a class";
		}
		throw new InternalErrorException("Unsupported parse result class: " + parseResultClass);
	}
}
