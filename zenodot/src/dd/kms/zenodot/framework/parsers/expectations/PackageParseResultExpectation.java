package dd.kms.zenodot.framework.parsers.expectations;

import dd.kms.zenodot.framework.result.PackageParseResult;

public class PackageParseResultExpectation extends AbstractParseResultExpectation<PackageParseResult>
{
	public PackageParseResultExpectation() {
		this(false);
	}

	private PackageParseResultExpectation(boolean parseWholeText) {
		super(PackageParseResult.class, parseWholeText);
	}

	@Override
	public PackageParseResultExpectation parseWholeText(boolean parseWholeText) {
		return isParseWholeText() == parseWholeText
			? this
			: new PackageParseResultExpectation(parseWholeText);
	}
}
