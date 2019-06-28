package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

/**
 * Parses expressions of the form {@code <package name>} in the context of {@code this} (ignored).
 */
public class RootpackageParser extends AbstractPackageParser<ObjectInfo>
{
	public RootpackageParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	String getPackagePrefix(ObjectInfo contextInfo) {
		return "";
	}
}
