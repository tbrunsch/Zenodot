package dd.kms.zenodot.api.common.multistringmatching;

import java.util.HashMap;
import java.util.Map;

/**
 * This class helps creating copies of {@link MultiStringMatcherNode}s including their links to other
 * nodes. Since a node {@code u} can be referenced by multiple nodes {@code v} and {@code w}, it is
 * important to ensure that the clones of {@code v} and {@code w} refer to the same clone of {@code u}.
 * This is why creating clones must be done by calling {@link #createClone(MultiStringMatcherNode)}
 * instead of directly calling {@link MultiStringMatcherNode#MultiStringMatcherNode(MultiStringMatcherNode, MultiStringMatcherNodeCloner)}
 * because this method ensures that only one clone will be generated per node.
 */
class MultiStringMatcherNodeCloner<T>
{
	private final Map<MultiStringMatcherNode<T>, MultiStringMatcherNode<T>> clonedNodes	= new HashMap<>();

	MultiStringMatcherNode<T> createClone(MultiStringMatcherNode<T> node) {
		MultiStringMatcherNode<T> clone = clonedNodes.get(node);
		if (clone == null) {
			clone = new MultiStringMatcherNode<>(node, this);
			clonedNodes.put(node, clone);
		}
		return clone;
	}
}
