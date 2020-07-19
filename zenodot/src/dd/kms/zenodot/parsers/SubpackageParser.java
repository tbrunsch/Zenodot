package dd.kms.zenodot.parsers;

import dd.kms.zenodot.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

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
