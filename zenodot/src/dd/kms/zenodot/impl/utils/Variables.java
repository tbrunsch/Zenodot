package dd.kms.zenodot.impl.utils;

import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class Variables
{
	private final @Nullable Variables	parentScope;

	private static final Map<String, ObjectInfo>	variables	= new HashMap<>();

	public Variables(@Nullable Variables parentScope) {
		this.parentScope = parentScope;
	}

	public @Nullable ObjectInfo getValue(String name) {
		ObjectInfo valueInfo = variables.get(name);
		if (valueInfo != null) {
			return valueInfo;
		}
		return parentScope != null ? parentScope.getValue(name) : null;
	}

	public void setValue(String name, ObjectInfo valueInfo) throws InternalErrorException {
		if (variables.containsKey(name)) {
			variables.put(name, valueInfo);
		} else if (parentScope != null) {
			parentScope.setValue(name, valueInfo);
		} else {
			// should be checked by caller
			throw new InternalErrorException("Unknown variable '" + name + "'");
		}
	}

	public void newVariable(String name, ObjectInfo valueInfo) throws InternalErrorException {
		// variables can only be introduced in the current scope
		if (variables.containsKey(name)) {
			// should be checked by caller
			throw new InternalErrorException("Variable '" + name + "' already exists");
		}
		variables.put(name, valueInfo);
	}
}
