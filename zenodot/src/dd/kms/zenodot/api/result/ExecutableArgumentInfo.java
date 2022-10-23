package dd.kms.zenodot.api.result;

import java.lang.reflect.Executable;
import java.util.Map;

/**
 * Contains information about the executable the caret is currently in.
 */
public interface ExecutableArgumentInfo
{
	/**
	 * Returns the index of the executable's argument the caret is currently on.
	 */
	int getCurrentArgumentIndex();

	/**
	 * Returns a map from all executable overloads to Boolean. An executable overload is mapped to true
	 * if and only if it might be applicable for the arguments that have already been parsed (until the caret).
	 */
	Map<Executable, Boolean> getApplicableExecutableOverloads();
}
