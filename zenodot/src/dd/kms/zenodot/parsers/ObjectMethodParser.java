package dd.kms.zenodot.parsers;

import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;

public class ObjectMethodParser extends AbstractMethodParser<ObjectInfo>
{
	public ObjectMethodParser(ParserToolbox parserToolbox, ObjectInfo thisInfo) {
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
	List<ExecutableInfo> getMethodInfos(ObjectInfo contextInfo) {
		TypeInfo contextType = parserToolbox.getObjectInfoProvider().getType(contextInfo);
		return parserToolbox.getInspectionDataProvider().getMethodInfos(contextType, false);
	}
}
