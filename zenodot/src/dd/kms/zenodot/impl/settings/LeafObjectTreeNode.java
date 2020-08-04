package dd.kms.zenodot.impl.settings;

import dd.kms.zenodot.api.settings.ObjectTreeNode;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.util.Collections;

/**
 * Implementation of {@link ObjectTreeNode} that represents a leaf in the custom hierarchy.
 */
public class LeafObjectTreeNode implements ObjectTreeNode
{
	private final String		name;
	private final ObjectInfo	userObject;

	public LeafObjectTreeNode(String name, ObjectInfo userObject) {
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
