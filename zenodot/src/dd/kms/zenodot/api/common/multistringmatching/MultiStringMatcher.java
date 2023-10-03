package dd.kms.zenodot.api.common.multistringmatching;

import dd.kms.zenodot.api.common.RegexUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class is intended to be used for searching in a fixed large list of strings
 * over and over again with different wildcard patterns. The result is supposed to be
 * the same as when filtering all contained strings with the regex returned by
 * {@link RegexUtils#createRegexForWildcardString(String)}.
 * However, it is optimized for this special type of regex pattern.
 */
public class MultiStringMatcher<T>
{
	private final MultiStringMatcherNode<T>	root 		= new MultiStringMatcherNode<>();
	private boolean							immutable;

	public void put(String key, T value) {
		if (immutable) {
			throw new UnsupportedOperationException("Trying to modify immutable " + getClass().getSimpleName());
		}
		if (value == null) {
			throw new IllegalArgumentException("The value must not be null");
		}
		/*
		 * Step 1: Create path
		 */
		MultiStringMatcherNode<T> node = root;
		int pathLength = key.length();
		List<MultiStringMatcherNode<T>> path = new ArrayList<>(pathLength);
		int lastExistingNodeIndex = -1;
		for (int i = 0; i < pathLength; i++) {
			char c = key.charAt(i);
			if (c == '*') {
				throw new IllegalArgumentException("Keys must not contain the wildcard character '*'");
			}
			MultiStringMatcherNode<T> child = node.getChild(c);
			if (child == null) {
				child = node.createChild(c);
			} else {
				lastExistingNodeIndex = i;
			}
			path.add(child);
			node = child;
		}
		node.addValue(value);

		/*
		 * Step 2: Create shortcuts for capital letters
		 */
		int lastCapitalLetterIndex = -1;
		char lastCapitalLetter = 0;
		for (int i = pathLength - 1; i >= 0; i--) {
			node = path.get(i);
			char c = key.charAt(i);
			/*
			 * Add shortcut if it is shorter than direct child connection:
			 * For ArrayList we create a shortcut for all nodes corresponding
			 * the the indices from "Arra" to the node corresponding to "L",
			 * but not from the node corresponding to the "y".
			 */
			if (lastCapitalLetterIndex > i + 1) {
				node.addShortcut(lastCapitalLetter, path.get(lastCapitalLetterIndex));
			}
			if (Character.isUpperCase(c)) {
				/*
				 * The predecessor indices will shortcut to here. If, however, the
				 * node had already existed, these shortcuts should already exist.
				 * The same holds for all predecessors, so there is nothing left
				 * to do.
				 */
				if (i <= lastExistingNodeIndex) {
					return;
				}
				lastCapitalLetterIndex = i;
				lastCapitalLetter = c;
			}
		}
	}

	public void makeImmutable() {
		this.immutable = true;
	}

	public Set<T> search(String keyPattern) {
		return search(keyPattern, Integer.MAX_VALUE);
	}

	public Set<T> search(String keyPattern, int maxNumberOfResults) {
		MultiStringWildcardSearch<T> wildcardSearch = new MultiStringWildcardSearch<>(keyPattern, root, maxNumberOfResults);
		return wildcardSearch.search();
	}
}
