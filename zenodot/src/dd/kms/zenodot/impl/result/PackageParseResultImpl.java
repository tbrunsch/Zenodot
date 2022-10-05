package dd.kms.zenodot.impl.result;

import dd.kms.zenodot.api.result.PackageParseResult;

class PackageParseResultImpl implements PackageParseResult
{
	private final String	packageName;

	PackageParseResultImpl(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public String getPackageName() {
		return packageName;
	}

	@Override
	public String toString() {
		return packageName;
	}
}
