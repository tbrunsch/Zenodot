package dd.kms.zenodot.settings;

import javax.annotation.Nullable;
import java.util.Collections;

/**
 * Implementation of {@link ObjectTreeNode} that represents a leaf in the custom hierarchy.
 */
public class LeafObjectTreeNode implements ObjectTreeNode
{
	static final LeafObjectTreeNode	EMPTY	= new LeafObjectTreeNode(null, null);

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
	public @Nullable Object getUserObject() {
		return userObject;
	}
}
