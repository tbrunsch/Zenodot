package dd.kms.zenodot.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dd.kms.zenodot.settings.Variable;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Manages a collection of variables. If requested (see {@link Variable#isUseHardReference()}),
 * then the pool will not reference certain variable values by hard references to allow garbage
 * collection.
 */
public class VariablePool
{
	private final ImmutableMap<String, ValueData> variables;

	public VariablePool(List<Variable> variables) {
		ImmutableMap.Builder<String, ValueData> variablesBuilder = ImmutableMap.builder();
		for (Variable variable : variables) {
			variablesBuilder.put(variable.getName(), new ValueData(variable.getValue(), variable.isUseHardReference()));
		}
		this.variables = variablesBuilder.build();
	}

	public List<Variable> getVariables() {
		ImmutableList.Builder<Variable> builder = ImmutableList.builder();
		for (String name : variables.keySet()) {
			ValueData valueData = variables.get(name);
			Object value = valueData.getValue();
			if (!valueData.isGarbageCollected()) {
				builder.add(new Variable(name, value, valueData.isUseHardReference()));
			}
		}
		return builder.build();
	}

	private static class ValueData
	{
		private final WeakReference<Object> weakValueReference;	// always set
		private final Object				hardValueReference;	// only set if user wants to save variables from being garbage collected
		private final boolean				valueIsNull;
		private final boolean				useHardReference;

		ValueData(Object value, boolean useHardReference) {
			weakValueReference = new WeakReference<>(value);
			hardValueReference = useHardReference ? value : null;
			valueIsNull = value == null;
			this.useHardReference = useHardReference;
		}

		boolean isUseHardReference() {
			return useHardReference;
		}

		Object getValue() {
			return weakValueReference.get();
		}

		boolean isGarbageCollected() {
			return getValue() == null && !valueIsNull;
		}
	}
}
