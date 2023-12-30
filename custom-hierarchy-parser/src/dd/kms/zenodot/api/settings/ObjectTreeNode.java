package dd.kms.zenodot.api.settings;

/**
 * Interface for all nodes in the custom hierarchy injected into the parser. See
 * {@link ParserSettingsBuilder#customHierarchyRoot(ObjectTreeNode)} for more
 * information.
 */
public interface ObjectTreeNode
{
	static ObjectTreeNode createLeafNode(String name, Object userObject) {
		return new dd.kms.zenodot.impl.settings.LeafObjectTreeNode(name, userObject);
	}

	String getName();
	Iterable<? extends ObjectTreeNode> getChildNodes();
	Object getUserObject();
}
