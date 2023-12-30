package dd.kms.zenodot.api;

import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

public interface CustomHierarchyParserExtension
{
	String	EXTENSION_NAME	= "Custom Hierarchy Parser";

	static CustomHierarchyParserExtension create(ObjectTreeNode customHierarchyRoot) {
		return new dd.kms.zenodot.impl.CustomHierarchyParserExtensionImpl(customHierarchyRoot);
	}

	CustomHierarchyParserExtension hierarchyCharacters(char hierarchyBegin, char hierarchySeparator, char hierarchyEnd);

	ParserSettingsBuilder configure(ParserSettingsBuilder parserSettingsBuilder);
}
