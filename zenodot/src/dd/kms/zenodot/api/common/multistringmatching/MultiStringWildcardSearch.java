package dd.kms.zenodot.api.common.multistringmatching;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MultiStringWildcardSearch<T>
{
	private final String					keyPattern;
	private final MultiStringMatcherNode<T>	root;
	private final int						maxNumberOfResults;
	private final Set<T>					results		= new HashSet<>();

	MultiStringWildcardSearch(String keyPattern, MultiStringMatcherNode<T> root, int maxNumberOfResults) {
		this.keyPattern = keyPattern;
		this.root = root;
		this.maxNumberOfResults = maxNumberOfResults;
	}

	Set<T> search() {
		try {
			search(0, root);
		} catch (EarlyTerminationException e) {
			/* This is no error. */
		}
		return results;
	}

	private void search(int startIndex, MultiStringMatcherNode<T> node) {
		if (startIndex == keyPattern.length()) {
			/*
			 * The pattern ends at this node => All results of this node and all
			 * of its children will match the pattern.
			 */
			collectResultsOfWholeTree(node);
			return;
		}
		char c = keyPattern.charAt(startIndex);
		/*
		 * Search in child
		 */
		MultiStringMatcherNode<T> child = node.getChild(c);
		if (child != null) {
			search(startIndex+1, child);
		}
		/*
		 * Search in shortcuts
		 */
		if (Character.isUpperCase(c)) {
			Collection<MultiStringMatcherNode<T>> shortcutNodes = node.getShortcuts(c);
			for (MultiStringMatcherNode<T> shortcutNode : shortcutNodes) {
				search(startIndex+1, shortcutNode);
			}
		}
		/*
		 * Wildcard search
		 */
		if (c == '*') {
			collectWildcardSearchResults(startIndex+1, node);
		}
	}

	private void collectResultsOfWholeTree(MultiStringMatcherNode<T> node) {
		List<T> values = node.getValues();
		if (!values.isEmpty()) {
			if (results.size() + values.size() < maxNumberOfResults) {
				results.addAll(values);
			} else {
				for (T value : values) {
					results.add(value);
					if (results.size() >= maxNumberOfResults) {
						/*
						 * Use an exception to break to regular search control flow.
						 */
						throw new EarlyTerminationException();
					}
				}
			}
		}
		Collection<MultiStringMatcherNode<T>> children = node.getChildren();
		for (MultiStringMatcherNode<T> child : children) {
			collectResultsOfWholeTree(child);
		}
	}

	private void collectWildcardSearchResults(int startIndex, MultiStringMatcherNode<T> node) {
		if (startIndex == keyPattern.length()) {
			/*
			 * The pattern ends at this node => All results of this node and all
			 * of its children will match the pattern.
			 */
			collectResultsOfWholeTree(node);
			return;
		}
		/*
		 * Match '*' with empty string
		 */
		search(startIndex, node);
		/*
		 * Match '*' with an arbitrary child letter and apply wildcard search again
		 */
		Collection<MultiStringMatcherNode<T>> children = node.getChildren();
		for (MultiStringMatcherNode<T> child : children) {
			collectWildcardSearchResults(startIndex, child);
		}
	}

	/**
	 * Used to break the regular search control flow. This exception does
	 * not indicate an error.
	 */
	private static class EarlyTerminationException extends RuntimeException {}
}
