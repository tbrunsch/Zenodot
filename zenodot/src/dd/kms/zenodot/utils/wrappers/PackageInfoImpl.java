package dd.kms.zenodot.utils.wrappers;

class PackageInfoImpl implements PackageInfo
{
	private final String	packageName;

	PackageInfoImpl(String packageName) {
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
