package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.PackageInfo;

/**
 * An instance of this interface is returned if the subexpression describes a package.
 */
public interface PackageParseResult extends ParseResult
{
	PackageInfo getPackage();
}
