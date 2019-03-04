package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;

/**
 * Parses a sub expression starting with a field {@code <field>}, assuming the context {@code <context class>.<field>}
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
	List<FieldInfo> getFieldInfos(TypeInfo contextType) {
		return parserToolbox.getInspectionDataProvider().getFieldInfos(contextType, true);
	}
}
