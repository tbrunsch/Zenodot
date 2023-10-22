package dd.kms.zenodot.api;

import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.settings.parsers.AdditionalParserSettings;

public class CustomHierarchyParsers
{
	public static AdditionalParserSettings createCustomHierarchyParserSettings(ObjectTreeNode customHierarchyRoot) {
		return createCustomHierarchyParserSettings(customHierarchyRoot, '{', '#', '}');
	}

	public static AdditionalParserSettings createCustomHierarchyParserSettings(ObjectTreeNode customHierarchyRoot, char hierarchyBegin, char hierarchySeparator, char hierarchyEnd) {
		return new dd.kms.zenodot.impl.settings.parsers.AdditionalCustomHierarchyParserSettings(customHierarchyRoot, hierarchyBegin, hierarchySeparator, hierarchyEnd);
	}

	public static ObjectTreeNode createLeafNode(String name, Object userObject) {
		return new dd.kms.zenodot.impl.settings.LeafObjectTreeNode(name, userObject);
	}
}
