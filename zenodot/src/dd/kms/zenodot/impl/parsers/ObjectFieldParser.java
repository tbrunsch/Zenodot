package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;
import dd.kms.zenodot.impl.utils.ParserToolbox;

/**
 * Parses
 * <ul>
 *     <li>
 *         expressions of the form {@code <field>} in the context of {@code this} and
 *     </li>
 *     <li>
 *         subexpressions {@code <field>} of expressions of the form {@code <instance>.<field>} with
 *         the instance as context.
 *     </li>
 * </ul>
 */
public class ObjectFieldParser extends AbstractFieldParser<ObjectInfo>
{
	public ObjectFieldParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
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
