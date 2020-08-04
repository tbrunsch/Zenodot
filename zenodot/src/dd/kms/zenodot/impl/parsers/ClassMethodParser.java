package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.api.wrappers.TypeInfo;
import dd.kms.zenodot.impl.utils.ParserToolbox;

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
