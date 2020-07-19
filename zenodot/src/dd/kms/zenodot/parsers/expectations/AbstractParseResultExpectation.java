package dd.kms.zenodot.parsers.expectations;

import dd.kms.zenodot.flowcontrol.InternalErrorException;
import dd.kms.zenodot.flowcontrol.InternalParseException;
import dd.kms.zenodot.result.ClassParseResult;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.result.PackageParseResult;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.tokenizer.TokenStream;
import dd.kms.zenodot.utils.dataproviders.ObjectInfoProvider;

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

	void doCheck(T parseResult, ObjectInfoProvider objectInfoProvider) throws InternalParseException {}

	@Override
	public final T check(TokenStream tokenStream, ParseResult parseResult, ObjectInfoProvider objectInfoProvider) throws InternalErrorException, InternalParseException {
		if (!expectationClazz.isInstance(parseResult)) {
			String error = "Expected " + getDescription(expectationClazz) + ", but obtained " + getDescription(parseResult.getClass());
			throw new InternalParseException(error);
		}
		T castedParseResult = expectationClazz.cast(parseResult);
		doCheck(castedParseResult, objectInfoProvider);

		if (parseWholeText) {
			char c = tokenStream.peekCharacter();
			if (c != TokenStream.EMPTY_CHARACTER) {
				throw new InternalParseException("Unexpected character '" + c + "'");
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
