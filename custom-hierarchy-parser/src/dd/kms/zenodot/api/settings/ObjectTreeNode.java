package dd.kms.zenodot.api.settings;

/**
 * Interface for all nodes in the custom hierarchy injected into the parser. See
 * {@link ParserSettingsBuilder#customHierarchyRoot(ObjectTreeNode)} for more
 * information.
 */
public interface ObjectTreeNode
{
	String getName();
	Iterable<? extends ObjectTreeNode> getChildNodes();
	Object getUserObject();
}
