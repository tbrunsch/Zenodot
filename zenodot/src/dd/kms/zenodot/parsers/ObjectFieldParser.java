package dd.kms.zenodot.parsers;

import dd.kms.zenodot.result.ParseResult;
import dd.kms.zenodot.result.ParseResultType;
import dd.kms.zenodot.result.AbstractCompiledParseResult;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

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
