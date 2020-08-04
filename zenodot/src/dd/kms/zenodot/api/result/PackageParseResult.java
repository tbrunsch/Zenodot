package dd.kms.zenodot.api.result;

import dd.kms.zenodot.api.wrappers.PackageInfo;

/**
 * An instance of this interface is returned if the subexpression describes a package.
 */
public interface PackageParseResult extends ParseResult
{
	PackageInfo getPackage();
}
