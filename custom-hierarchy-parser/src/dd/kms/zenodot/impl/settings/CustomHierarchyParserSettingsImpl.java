package dd.kms.zenodot.impl.settings;

import dd.kms.zenodot.api.settings.CustomHierarchyParserSettings;
import dd.kms.zenodot.api.settings.ObjectTreeNode;

public class CustomHierarchyParserSettingsImpl implements CustomHierarchyParserSettings
{
	private final ObjectTreeNode	root;
	private final char				hierarchyBegin;
	private final char				hierarchySeparator;
	private final char				hierarchyEnd;

	public CustomHierarchyParserSettingsImpl(ObjectTreeNode root, char hierarchyBegin, char hierarchySeparator, char hierarchyEnd) {
		this.root = root;
		this.hierarchyBegin = hierarchyBegin;
		this.hierarchySeparator = hierarchySeparator;
		this.hierarchyEnd = hierarchyEnd;
	}

	@Override
	public ObjectTreeNode getRoot() {
		return root;
	}

	@Override
	public char getHierarchyBegin() {
		return hierarchyBegin;
	}

	@Override
	public char getHierarchySeparator() {
		return hierarchySeparator;
	}

	@Override
	public char getHierarchyEnd() {
		return hierarchyEnd;
	}
}
