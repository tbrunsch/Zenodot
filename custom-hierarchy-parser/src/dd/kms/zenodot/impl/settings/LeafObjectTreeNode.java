package dd.kms.zenodot.impl.settings;

import dd.kms.zenodot.api.settings.ObjectTreeNode;

import java.util.Collections;

/**
 * Implementation of {@link ObjectTreeNode} that represents a leaf in the custom hierarchy.
 */
public class LeafObjectTreeNode implements ObjectTreeNode
{
	private final String	name;
	private final Object	userObject;

	public LeafObjectTreeNode(String name, Object userObject) {
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
	public Object getUserObject() {
		return userObject;
	}
}
