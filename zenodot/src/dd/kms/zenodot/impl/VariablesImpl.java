package dd.kms.zenodot.impl;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.impl.flowcontrol.InternalErrorException;
import dd.kms.zenodot.impl.wrappers.InfoProvider;
import dd.kms.zenodot.impl.wrappers.ObjectInfo;

import javax.annotation.Nullable;
import java.util.*;

public class VariablesImpl implements Variables
{
	private final @Nullable VariablesImpl	parentScope;

	private final Map<String, ObjectInfo>	variables	= new HashMap<>();

	public VariablesImpl() {
		this(null);
	}

	public VariablesImpl(@Nullable VariablesImpl parentScope) {
		this.parentScope = parentScope;
	}

	@Override
	public Object getValue(String name) {
		ObjectInfo valueInfo = getValueInfo(name);
		return valueInfo.getObject();
	}

	public ObjectInfo getValueInfo(String name) {
		ObjectInfo valueInfo = variables.get(name);
		if (valueInfo != null) {
			return valueInfo;
		}
		if (parentScope != null) {
			return parentScope.getValueInfo(name);
		}
		throw new IllegalArgumentException("Variable '" + name + "' does not exist");
	}

	@Override
	public Variables createVariable(String name, Object value, boolean isFinal) {
		ObjectInfo valueInfo = InfoProvider.createObjectInfo(value);
		try {
			createVariable(name, valueInfo, isFinal);
		} catch (InternalErrorException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		return this;
	}

	public void createVariable(String name, ObjectInfo valueInfo, boolean isFinal) throws InternalErrorException {
		// variables can only be introduced in the current scope
		if (variables.containsKey(name)) {
			// should be checked by caller
			throw new InternalErrorException("Variable '" + name + "' already exists");
		}
		doCreateVariable(name, valueInfo, isFinal);
	}

	private void doCreateVariable(String name, ObjectInfo valueInfo, boolean isFinal) {
		ObjectInfo.ValueSetter setter = !isFinal
			? newValueInfo -> setValueInfo(name, newValueInfo)
			: null;
		ObjectInfo settableVariableInfo = new ObjectInfo(valueInfo.getObject(), valueInfo.getDeclaredType(), setter);
		variables.put(name, settableVariableInfo);
	}

	private void setValueInfo(String name, ObjectInfo valueInfo) {
		// this method is always called in the scope the variable is declared in
		ObjectInfo variableInfo = variables.get(name);
		if (variableInfo == null) {
			throw new IllegalStateException("Unknown variable '" + name + "'");
		}
		ObjectInfo newVariableInfo = new ObjectInfo(valueInfo.getObject(), valueInfo.getDeclaredType(), variableInfo.getValueSetter());
		variables.put(name, newVariableInfo);
	}

	@Override
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
