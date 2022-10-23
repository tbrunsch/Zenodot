package dd.kms.zenodot.impl.result;

/**
 * An instance of this interface is returned if the subexpression describes a package.
 */
public interface PackageParseResult extends ParseResult
{
	String getPackageName();
}
