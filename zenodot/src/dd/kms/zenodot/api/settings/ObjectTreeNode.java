package dd.kms.zenodot.api.settings;

import dd.kms.zenodot.api.wrappers.ObjectInfo;

/**
 * Interface for all nodes in the custom hierarchy injected into the parser. See
 * {@link ParserSettingsBuilder#customHierarchyRoot(ObjectTreeNode)} for more
 * information.
 */
public interface ObjectTreeNode
{
	String getName();
	Iterable<? extends ObjectTreeNode> getChildNodes();
	ObjectInfo getUserObject();
}
