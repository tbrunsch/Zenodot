package dd.kms.zenodot.impl.result;

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
