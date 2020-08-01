package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.result.ParseResult;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.parsers.expectations.ParseResultExpectation;
import dd.kms.zenodot.impl.utils.ParserToolbox;

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
