package dd.kms.zenodot.api;

import java.util.Collection;

/**
 * A modifiable collection of variables. Although this is an interface, you must not
 * implement it your own, but use methods like {@link #create()} or
 * {@link #of(String, Object)} to create instances of this interface.
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
	 * @return A variable collection with a single variable with the given {@code name}
	 * and the given {@code value}.
	 */
	static Variables of(String name, Object value) {
		return create().createVariable(name, value);
	}

	/**
	 * @return A variable collection with two variables: one with name {@code name1} and
	 * value {@code value1}, one with name {@code name2} and value {@code value2}, and
	 * one with name {@code name3} and value {@code value3}.
	 */
	static Variables of(String name1, Object value1, String name2, Object value2) {
		return create().createVariable(name1, value1).createVariable(name2, value2);
	}

	/**
	 * @return A variable collection with three variables: one with name {@code name1} and
	 * value {@code value1} and one with name {@code name2} and value {@code value2}.
	 */
	static Variables of(String name1, Object value1, String name2, Object value2, String name3, Object value3) {
		return create().createVariable(name1, value1).createVariable(name2, value2).createVariable(name3, value3);
	}

	/**
	 * Creates a variable with the given {@code name} and the given {@code value} and adds it
	 * to the collection of variables.
	 * @return the modified collection
	 * @throws IllegalArgumentException when a variable with that name already exists
	 */
	Variables createVariable(String name, Object value);

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
