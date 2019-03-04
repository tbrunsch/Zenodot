package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.FieldInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;

/**
 * Parses a sub expression starting with a field {@code <field>}, assuming the context
 * <ul>
 *     <li>{@code <context instance>.<field>} or</li>
 *     <li>{@code <field>} (like {@code <context instance>.<field>} for {@code <context instance> = this})</li>
 * </ul>
 */
public class ObjectFieldParser extends AbstractFieldParser<ObjectInfo>
{
	public ObjectFieldParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
		super(parserToolbox, thisInfo);
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
	List<FieldInfo> getFieldInfos(ObjectInfo contextInfo) {
		TypeInfo contextType = parserToolbox.getObjectInfoProvider().getType(contextInfo);
		return parserToolbox.getInspectionDataProvider().getFieldInfos(contextType, false);
	}
}
