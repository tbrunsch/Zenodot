package dd.kms.zenodot.api;

import java.util.Collection;

/**
 * A modifiable collection of variables. Although this is an interface, you must not implement it your own,
 * but use the method {@link #create()} to create instances of this interface.
 */
public interface Variables
{
	/**
	 * @return An empty variable collection.
	 */
	static Variables create() {
		return new dd.kms.zenodot.impl.VariablesImpl();
	}

	/**
	 * Creates a variable with the given {@code name} and the given {@code value} and adds it
	 * to the collection of variables. The parameter {@code isFinal} specifies whether the
	 * variable if {@code final} or whether its value can be overwritten.
	 * @return the modified collection
	 * @throws IllegalArgumentException when a variable with that name already exists
	 */
	Variables createVariable(String name, Object value, boolean isFinal);

	/**
	 * @return The value of the variable with the given {@code name}
	 * @throws IllegalArgumentException if no variable with this name exists
	 */
	Object getValue(String name);

	/**
	 * @return The names of the variables contained in this variable collection
	 */
	Collection<String> getNames();
}
