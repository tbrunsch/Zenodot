package dd.kms.zenodot.common.multistringmatching;

import java.util.*;

import javax.annotation.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

class MultiStringMatcherNode<T>
{
	/**
	 * Values associated with that node. Values will be set for those nodes
	 * that represent the last character of a registered key.<br/>
	 * <br/>
	 * Example: If the key "ArrayList" has been registered, then there will be
	 * one node for each of the character indices of "ArrayList". The node that
	 * is associated with the last index (corresponding to the letter "t") contains
	 * the value registered for the key "ArrayList".
	 *
	 */
	private final List<T>	 										values			= new ArrayList<>();

	/**
	 * Stores the children of that node corresponding to the successor character.
	 * For example, if the keys "ArrayList" and "ArrayBlockingQueue"
	 * the child relation will look as follows:<br/>
	 * <br/>
	 * <pre>
	 *     A-r-r-a-y-L-i-s-t
	 *             |
	 *             B-l-o-c-k-i-n-g-Q-u-e-u-e
	 * </pre>
	 * Both keys share the path corresponding to "Array" and then have there custom
	 * subpaths corresponding to "List" and "BlockingQueue".
	 */
	private final Map<Character, MultiStringMatcherNode<T>>			children		= new HashMap<>();

	/**
	 * Stores the shortcuts of that node corresponding to upper case successor characters.
	 * For example, for the key "ArrayList" there will be shortcuts from all nodes
	 * corresponding to "Arra" to the node corresponding to "L" registered for the character
	 * "L". That way, it is possible to jump to the "L" by typing the key pattern "ArL".
	 */
	private final Multimap<Character, MultiStringMatcherNode<T>>	shortcutNodes	= ArrayListMultimap.create();

	void addValue(T value) {
		values.add(value);
	}

	List<T> getValues() {
		return values;
	}

	@Nullable MultiStringMatcherNode<T> getChild(char c) {
		return children.get(c);
	}

	MultiStringMatcherNode<T> createChild(char c) {
		MultiStringMatcherNode<T> child = new MultiStringMatcherNode<>();
		if (children.put(c, child) != null) {
			throw new IllegalArgumentException("There was already a child associated with character '" + c + "'");
		}
		return child;
	}

	Collection<MultiStringMatcherNode<T>> getChildren() {
		return children.values();
	}

	void addShortcut(char c, MultiStringMatcherNode<T> shortcutNode) {
		shortcutNodes.put(c, shortcutNode);
	}

	Collection<MultiStringMatcherNode<T>> getShortcuts(char c) {
		return shortcutNodes.get(c);
	}
}
