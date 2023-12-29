package dd.kms.zenodot.impl.settings;

import dd.kms.zenodot.api.settings.ObjectTreeNode;

public class CustomHierarchyParserSettings
{
	private final ObjectTreeNode	root;
	private final char				hierarchyBegin;
	private final char				hierarchySeparator;
	private final char				hierarchyEnd;

	public CustomHierarchyParserSettings(ObjectTreeNode root, char hierarchyBegin, char hierarchySeparator, char hierarchyEnd) {
		this.root = root;
		this.hierarchyBegin = hierarchyBegin;
		this.hierarchySeparator = hierarchySeparator;
		this.hierarchyEnd = hierarchyEnd;
	}

	public ObjectTreeNode getRoot() {
		return root;
	}

	public char getHierarchyBegin() {
		return hierarchyBegin;
	}

	public char getHierarchySeparator() {
		return hierarchySeparator;
	}

	public char getHierarchyEnd() {
		return hierarchyEnd;
	}
}
