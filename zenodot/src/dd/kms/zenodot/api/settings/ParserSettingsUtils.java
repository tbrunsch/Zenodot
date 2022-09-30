package dd.kms.zenodot.api.settings;

import dd.kms.zenodot.api.wrappers.InfoProvider;

public class ParserSettingsUtils
{
	private static final ObjectTreeNode EMPTY_LEAF_NODE	= new dd.kms.zenodot.impl.settings.LeafObjectTreeNode(null, null);

	public static Variable createVariable(String name, Object value, boolean useHardReference) {
		return new dd.kms.zenodot.impl.settings.VariableImpl(name, InfoProvider.createObjectInfo(value), useHardReference);
	}

	public static Variable createVariable(String name, Object value, Class<?> declaredType, boolean useHardReference) {
		return new dd.kms.zenodot.impl.settings.VariableImpl(name, InfoProvider.createObjectInfo(value, declaredType), useHardReference);
	}

	public static ObjectTreeNode createEmptyLeafNode() {
		return EMPTY_LEAF_NODE;
	}

	public static ObjectTreeNode createLeafNode(String name, Object userObject) {
		return new dd.kms.zenodot.impl.settings.LeafObjectTreeNode(name, InfoProvider.createObjectInfo(userObject));
	}

	public static ObjectTreeNode createLeafNode(String name, Object userObject, Class<?> declaredType) {
		return new dd.kms.zenodot.impl.settings.LeafObjectTreeNode(name, InfoProvider.createObjectInfo(userObject, declaredType));
	}
}
