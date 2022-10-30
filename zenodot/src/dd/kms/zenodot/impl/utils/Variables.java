package dd.kms.zenodot.impl.utils;

import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import java.util.*;

public class Variables
{
	private final @Nullable Variables		parentScope;

	private final Map<String, ObjectInfo>	variables	= new HashMap<>();

	public Variables(@Nullable Variables parentScope) {
		this.parentScope = parentScope;
	}

	public @Nullable ObjectInfo getVariable(String name) {
		ObjectInfo valueInfo = variables.get(name);
		if (valueInfo != null) {
			return valueInfo;
		}
		return parentScope != null ? parentScope.getVariable(name) : null;
	}

	public void createVariable(String name, ObjectInfo valueInfo) throws InternalErrorException {
		// variables can only be introduced in the current scope
		if (variables.containsKey(name)) {
			// should be checked by caller
			throw new InternalErrorException("Variable '" + name + "' already exists");
		}
		doCreateVariable(name, valueInfo);
	}

	private void doCreateVariable(String name, ObjectInfo valueInfo) {
		/*
		 * We allow setting variables in inner scopes, but not in the root scope. The root scope contains the values of
		 * the variables defined in the immutable settings specified by the user. Setting them would have no effect on
		 * the originally specified variables.
		 * Inner scopes will be used by the lambda parser and will be required when supporting the evaluation of whole
		 * code fragments.
		 */
		ObjectInfo.ValueSetter setter = parentScope != null
			? newValueInfo -> setVariableValue(name, newValueInfo)
			: null;
		ObjectInfo settableVariableInfo = new ObjectInfo(valueInfo.getObject(), valueInfo.getDeclaredType(), setter);
		variables.put(name, settableVariableInfo);
	}

	private void setVariableValue(String name, ObjectInfo valueInfo) {
		// this method is always called in the scope the variable is declared in
		if (!variables.containsKey(name)) {
			throw new IllegalStateException("Unknown variable '" + name + "'");
		}
		variables.remove(name);
		doCreateVariable(name, valueInfo);
	}

	public Collection<String> getNames() {
		Set<String> names = new HashSet<>();
		addNames(names);
		return names;
	}

	private void addNames(Set<String> names) {
		names.addAll(variables.keySet());
		if (parentScope != null) {
			parentScope.addNames(names);
		}
	}
}
