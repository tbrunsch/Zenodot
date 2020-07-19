package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.PackageInfo;

class PackageParseResultImpl implements PackageParseResult
{
	private final PackageInfo	packageInfo;

	PackageParseResultImpl(PackageInfo packageInfo) {
		this.packageInfo = packageInfo;
	}

	@Override
	public PackageInfo getPackage() {
		return packageInfo;
	}

	@Override
	public String toString() {
		return packageInfo.toString();
	}
}
