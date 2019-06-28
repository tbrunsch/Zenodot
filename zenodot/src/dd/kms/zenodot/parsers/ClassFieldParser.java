package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;

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
