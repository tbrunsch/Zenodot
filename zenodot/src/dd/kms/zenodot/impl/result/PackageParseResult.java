package dd.kms.zenodot.impl.result;

/**
 * An instance of this class is returned if the subexpression describes a package.
 */
public class PackageParseResult implements ParseResult
{
	private final String	packageName;

	PackageParseResult(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageName() {
		return packageName;
	}

	@Override
	public String toString() {
		return packageName;
	}
}
