package dd.kms.zenodot.api.settings;

public class ParserSettingsUtils
{
	private static final ObjectTreeNode EMPTY_LEAF_NODE	= new dd.kms.zenodot.impl.settings.LeafObjectTreeNode(null, null);

	public static ObjectTreeNode createEmptyLeafNode() {
		return EMPTY_LEAF_NODE;
	}

	public static ObjectTreeNode createLeafNode(String name, Object userObject) {
		return new dd.kms.zenodot.impl.settings.LeafObjectTreeNode(name, userObject);
	}
}
