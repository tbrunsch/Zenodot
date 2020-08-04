package dd.kms.zenodot.impl.wrappers;

import dd.kms.zenodot.api.wrappers.PackageInfo;

public class PackageInfoImpl implements PackageInfo
{
	private final String	packageName;

	public PackageInfoImpl(String packageName) {
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
