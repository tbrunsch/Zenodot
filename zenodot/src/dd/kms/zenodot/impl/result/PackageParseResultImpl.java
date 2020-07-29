package dd.kms.zenodot.impl.result;

import dd.kms.zenodot.api.result.PackageParseResult;
import dd.kms.zenodot.api.wrappers.PackageInfo;

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
