package dd.kms.zenodot.settings;

import javax.annotation.Nullable;

/**
 * Interface for all nodes in the custom hierarchy injected into the parser. See
 * {@link ParserSettingsBuilder#customHierarchyRoot(ObjectTreeNode)} for more
 * information.
 */
public interface ObjectTreeNode
{
	String getName();
	Iterable<ObjectTreeNode> getChildNodes();
	@Nullable Object getUserObject();
}
