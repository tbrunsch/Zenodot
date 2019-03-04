package dd.kms.zenodot.settings;

import javax.annotation.Nullable;

/**
 * Interface for all nodes in the custom hierarchy injected into the parser. See
 * {@link ParserSettingsBuilder#customHierarchyRoot(ObjectTreeNodeIF)} for more
 * information.
 */
public interface ObjectTreeNodeIF
{
	String getName();
	Iterable<ObjectTreeNodeIF> getChildNodes();
	@Nullable Object getUserObject();
}
