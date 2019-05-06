package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.AbstractExecutableInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;

/**
 * Parses subexpressions {@code <static method>(<arguments>)} of expressions of the form {@code <class>.<static method>(<arguments>)}.
 * The class {@code <class>} is the context for the parser.
 */
public class ClassMethodParser extends AbstractMethodParser<TypeInfo>
{
	public ClassMethodParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
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
	boolean isContextStatic() {
		return true;
	}

	@Override
	List<AbstractExecutableInfo> getMethodInfos(TypeInfo contextType) {
		return parserToolbox.getInspectionDataProvider().getMethodInfos(contextType, true);
	}
}
