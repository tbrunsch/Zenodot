package dd.kms.zenodot.impl.parsers;

import dd.kms.zenodot.framework.utils.ParserToolbox;

/**
 * Parses subexpressions {@code <static method>(<arguments>)} of expressions of the form {@code <class>.<static method>(<arguments>)}.
 * The class {@code <class>} is the context for the parser.
 */
public class ClassMethodParser extends AbstractMethodParser<Class<?>>
{
	public ClassMethodParser(ParserToolbox parserToolbox) {
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
