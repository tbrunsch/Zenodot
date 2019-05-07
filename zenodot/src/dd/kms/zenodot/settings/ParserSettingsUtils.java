package dd.kms.zenodot.settings;

public class ParserSettingsUtils
{
	private static final ObjectTreeNode	EMPTY_LEAF_NODE	= new LeafObjectTreeNode(null, null);

	public static ParserSettingsBuilder createBuilder() {
		return new ParserSettingsBuilderImpl();
	}

	public static Variable createVariable(String name, Object value, boolean useHardReference) {
		return new VariableImpl(name, value, useHardReference);
	}

	public static ObjectTreeNode createEmptyLeafNode() {
		return EMPTY_LEAF_NODE;
	}

	public static ObjectTreeNode createLeafNode(String name, Object userObject) {
		return new LeafObjectTreeNode(name, userObject);
	}
}
