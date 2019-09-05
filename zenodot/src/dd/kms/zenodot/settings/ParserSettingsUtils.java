package dd.kms.zenodot.settings;

import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

public class ParserSettingsUtils
{
	private static final ObjectTreeNode	EMPTY_LEAF_NODE	= new LeafObjectTreeNode(null, null);

	public static ParserSettingsBuilder createBuilder() {
		return new ParserSettingsBuilderImpl();
	}

	public static Variable createVariable(String name, Object value, boolean useHardReference) {
		return new VariableImpl(name, InfoProvider.createObjectInfo(value), useHardReference);
	}

	public static Variable createVariable(String name, Object value, TypeInfo declaredType, boolean useHardReference) {
		return new VariableImpl(name, InfoProvider.createObjectInfo(value, declaredType), useHardReference);
	}

	public static ObjectTreeNode createEmptyLeafNode() {
		return EMPTY_LEAF_NODE;
	}

	public static ObjectTreeNode createLeafNode(String name, Object userObject) {
		return new LeafObjectTreeNode(name, InfoProvider.createObjectInfo(userObject));
	}

	public static ObjectTreeNode createLeafNode(String name, Object userObject, TypeInfo declaredType) {
		return new LeafObjectTreeNode(name, InfoProvider.createObjectInfo(userObject, declaredType));
	}
}
