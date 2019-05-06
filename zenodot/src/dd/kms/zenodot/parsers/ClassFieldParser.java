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
	public ClassFieldParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
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
	List<FieldInfo> getFieldInfos(TypeInfo contextType) {
		return parserToolbox.getInspectionDataProvider().getFieldInfos(contextType, true);
	}
}
