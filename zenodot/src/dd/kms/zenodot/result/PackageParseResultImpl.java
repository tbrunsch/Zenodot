package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.PackageInfo;

class PackageParseResultImpl implements PackageParseResult
{
	private final int			position; 	// exclusive
	private final PackageInfo	packageInfo;

	PackageParseResultImpl(int position, PackageInfo packageInfo) {
		this.position = position;
		this.packageInfo = packageInfo;
	}

	@Override
	public ParseOutcomeType getOutcomeType() {
		return ParseOutcomeType.PACKAGE_PARSE_RESULT;
	}

	@Override
	public PackageInfo getPackage() {
		return packageInfo;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return "Parsed until " + position + ": " + packageInfo;
	}
}
