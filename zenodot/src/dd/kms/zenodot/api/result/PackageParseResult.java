package dd.kms.zenodot.api.result;

/**
 * An instance of this interface is returned if the subexpression describes a package.
 */
public interface PackageParseResult extends ParseResult
{
	String getPackageName();
}
