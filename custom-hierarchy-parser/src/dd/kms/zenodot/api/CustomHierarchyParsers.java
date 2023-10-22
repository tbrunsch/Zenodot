package dd.kms.zenodot.api;

import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.settings.parsers.AdditionalParserSettings;

public class CustomHierarchyParsers
{
	public static AdditionalParserSettings createCustomHierarchyParserSettings(ObjectTreeNode customHierarchyRoot) {
		return new dd.kms.zenodot.impl.settings.parsers.AdditionalCustomHierarchyParserSettings(customHierarchyRoot);
	}

	public static ObjectTreeNode createLeafNode(String name, Object userObject) {
		return new dd.kms.zenodot.impl.settings.LeafObjectTreeNode(name, userObject);
	}
}
