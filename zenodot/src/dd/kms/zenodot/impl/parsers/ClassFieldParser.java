package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.framework.utils.ParserToolbox;

/**
 * Parses subexpressions {@code <static field>} of expressions of the form {@code <class>.<static field>}.
 * The class {@code <class>} is the context for the parser.
 */
public class ClassFieldParser extends AbstractFieldParser<Class<?>>
{
	public ClassFieldParser(ParserToolbox parserToolbox) {
		super(parserToolbox);
	}

	@Override
	Object getContextObject(Class<?> context) {
		return null;
	}

	@Override
	Class<?> getContextType(Class<?> context) {
		return context;
	}

	@Override
	boolean isContextStatic() {
		return true;
	}
}
