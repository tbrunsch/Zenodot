package dd.kms.zenodot.settings;

import javax.annotation.Nullable;

public interface ObjectTreeNodeIF
{
	String getName();
	Iterable<ObjectTreeNodeIF> getChildNodes();
	@Nullable
	Object getUserObject();
}
