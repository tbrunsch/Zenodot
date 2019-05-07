package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;

/**
 * Parses
 * <ul>
 *     <li>
 *         expressions of the form {@code <method>(<arguments>)} in the context of {@code this} and
 *     </li>
 *     <li>
 *         subexpressions {@code <method>(<arguments>)} of expressions of the form {@code <instance>.<method>(<arguments>)}
 *         with the instance as context.
 *     </li>
 * </ul>
 */
public class ObjectMethodParser extends AbstractMethodParser<ObjectInfo>
{
	public ObjectMethodParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
	}

	@Override
	boolean contextCausesNullPointerException(ObjectInfo contextInfo) {
		return contextInfo.getObject() == null;
	}

	@Override
	Object getContextObject(ObjectInfo contextInfo) {
		return contextInfo.getObject();
	}

	@Override
	boolean isContextStatic() {
		return false;
	}

	@Override
	List<ExecutableInfo> getMethodInfos(ObjectInfo contextInfo) {
		TypeInfo contextType = parserToolbox.getObjectInfoProvider().getType(contextInfo);
		return parserToolbox.getInspectionDataProvider().getMethodInfos(contextType, false);
	}
}
