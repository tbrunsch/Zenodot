package dd.kms.zenodot.settings;

import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.Collections;

/**
 * Implementation of {@link ObjectTreeNode} that represents a leaf in the custom hierarchy.
 */
class LeafObjectTreeNode implements ObjectTreeNode
{
	private final String		name;
	private final ObjectInfo	userObject;

	LeafObjectTreeNode(String name, ObjectInfo userObject) {
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
	public ObjectInfo getUserObject() {
		return userObject;
	}
}
