package dd.kms.zenodot.settings;

import javax.annotation.Nullable;
import java.util.List;

public interface ObjectTreeNodeIF
{
	String getName();
	List<ObjectTreeNodeIF> getChildNodes();
	@Nullable
	Object getUserObject();
}
