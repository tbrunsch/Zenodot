package dd.kms.zenodot.settings;

import javax.annotation.Nullable;
import java.util.Collections;

/**
 * Implementation of {@link ObjectTreeNode} that represents a leaf in the custom hierarchy.
 */
class LeafObjectTreeNode implements ObjectTreeNode
{
	private final String	name;
	private final Object	userObject;

	LeafObjectTreeNode(String name, Object userObject) {
		this.name = name;
		this.userObject = userObject;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Iterable<ObjectTreeNode> getChildNodes() {
		return Collections.emptyList();
	}

	@Override
	public @Nullable Object getUserObject() {
		return userObject;
	}
}
