package dd.kms.zenodot.impl.settings;

import dd.kms.zenodot.api.settings.CustomHierarchyParserSettings;
import dd.kms.zenodot.api.settings.ObjectTreeNode;

public class CustomHierarchyParserSettingsImpl implements CustomHierarchyParserSettings
{
	private final ObjectTreeNode	root;

	public CustomHierarchyParserSettingsImpl(ObjectTreeNode root) {
		this.root = root;
	}

	@Override
	public ObjectTreeNode getRoot() {
		return root;
	}
}
