package dd.kms.zenodot.framework.parsers.expectations;

import dd.kms.zenodot.framework.result.ClassParseResult;

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
