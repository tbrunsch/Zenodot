package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.impl.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.api.result.ParseResult;
import dd.kms.zenodot.impl.utils.ParserToolbox;
import dd.kms.zenodot.api.wrappers.PackageInfo;

/**
 * Parses subexpressions {@code <package name>} of expressions of the form {@code <parent package name>.<package name>.
 * The package {@code <parent package name>} is the context for the parser.
 */
public class SubpackageParser<T extends ParseResult, S extends ParseResultExpectation<T>> extends AbstractPackageParser<PackageInfo, T, S>
{
	public SubpackageParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	String getPackagePrefix(PackageInfo context) {
		return context.getPackageName() + ".";
	}
}
