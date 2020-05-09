package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * Parses subexpressions {@code <static method>(<arguments>)} of expressions of the form {@code <class>.<static method>(<arguments>)}.
 * The class {@code <class>} is the context for the parser.
 */
public class ClassMethodParser extends AbstractMethodParser<TypeInfo>
{
	public ClassMethodParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	boolean contextCausesNullPointerException(TypeInfo contextType) {
		return false;
	}

	@Override
	Object getContextObject(TypeInfo context) {
		return null;
	}

	@Override
	TypeInfo getContextType(TypeInfo context) {
		return context;
	}

	@Override
	boolean isContextStatic() {
		return true;
	}
}
