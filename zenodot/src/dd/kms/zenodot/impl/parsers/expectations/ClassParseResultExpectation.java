package dd.kms.zenodot.impl.parsers.expectations;

import dd.kms.zenodot.impl.result.ClassParseResult;

public class ClassParseResultExpectation extends AbstractParseResultExpectation<ClassParseResult>
{
	public ClassParseResultExpectation() {
		this(false);
	}

	private ClassParseResultExpectation(boolean parseWholeText) {
		super(ClassParseResult.class, parseWholeText);
	}

	@Override
	public ClassParseResultExpectation parseWholeText(boolean parseWholeText) {
		return isParseWholeText() == parseWholeText
				? this
				: new ClassParseResultExpectation(parseWholeText);
	}
}
