package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

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
	public ObjectMethodParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
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
	TypeInfo getContextType(ObjectInfo context) {
		return parserToolbox.getObjectInfoProvider().getType(context);
	}

	@Override
	boolean isContextStatic() {
		return false;
	}
}
