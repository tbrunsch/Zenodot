package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.java.result.InstanceParseResult;

public class ArrayAccessInfo
{
	private final InstanceParseResult	array;
	private final InstanceParseResult	index;

	public ArrayAccessInfo(InstanceParseResult array, InstanceParseResult index) {
		this.array = array;
		this.index = index;
	}

	public InstanceParseResult getArray() {
		return array;
	}

	public InstanceParseResult getIndex() {
		return index;
	}
}
