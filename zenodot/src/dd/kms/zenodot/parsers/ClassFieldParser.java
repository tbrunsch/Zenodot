package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

/**
 * Parses subexpressions {@code <static field>} of expressions of the form {@code <class>.<static field>}.
 * The class {@code <class>} is the context for the parser.
 */
public class ClassFieldParser extends AbstractFieldParser<TypeInfo>
{
	public ClassFieldParser(ParserToolbox parserToolbox) {
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
