package dd.kms.zenodot.parsers;

import dd.kms.zenodot.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

/**
 * Parses expressions of the form {@code <package name>} in the context of {@code this} (ignored).
 */
public class RootpackageParser<T extends ParseResult, S extends ParseResultExpectation<T>> extends AbstractPackageParser<ObjectInfo, T, S>
{
	public RootpackageParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	String getPackagePrefix(ObjectInfo context) {
		return "";
	}
}
