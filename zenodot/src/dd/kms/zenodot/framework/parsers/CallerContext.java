package dd.kms.zenodot.framework.parsers;

import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.framework.wrappers.InfoProvider;

import java.lang.reflect.Executable;
import java.util.*;

public class CallerContext
{
	private final Set<Executable>	executables;

	/**
	 * Values of the previous parameters of the given {@link #executables}. Note that these
	 * values can be {@link InfoProvider#INDETERMINATE_VALUE} in case they are not known
	 * at this point in time.
	 */
	private final List<Object>		previousParameters;

	/**
	 * Can be one of the following values:
	 * <ul>
	 *     <li>
	 *         {@code null} if {@link #executables} are constructors or static methods
	 *     </li>
	 *     <li>
	 *         {@link InfoProvider#INDETERMINATE_VALUE} if {@code executables} are non-static
	 *         methods, but the instance is not known at this point in time.
	 *     </li>
	 *     <li>
	 *         Something else if {@code executables} are non-static methods and the instance is known.
	 *     </li>
	 * </ul>
	 */
	private final Object			caller;

	public CallerContext(Collection<Executable> executables, List<Object> previousParameters, Object caller) {
		this.executables = ImmutableSet.copyOf(executables);
		this.previousParameters = new ArrayList<>(previousParameters);
		this.caller = caller;
	}

	/**
	 * Returns the executables (methods or constructors) of which the current parser
	 * is parsing a parameter.
	 */
	public Set<Executable> getExecutables() {
		return executables;
	}

	/**
	 * Returns the values of the previous parameters of the the executables (methods
	 * or constructors) before the parameter the current parser is parsing. These
	 * values could be {@link InfoProvider#INDETERMINATE_VALUE} if they are not known
	 * at this point in time.
	 */
	public List<Object> getPreviousParameters() {
		return Collections.unmodifiableList(previousParameters);
	}

	/**
	 * Returns the value of the parameter with index {@code paramIndex}, casted to its expected
	 * type {@code paramType}.
	 * @throws IllegalStateException if the parameter value is undetermined or not of the expected type
	 * @throws IndexOutOfBoundsException if no parameter with the given index exists
	 */
	public <T> T getParameter(int paramIndex, Class<T> paramType, String paramDescription) {
		Object paramValue = previousParameters.get(paramIndex);
		if (paramValue == InfoProvider.INDETERMINATE_VALUE) {
			throw new IllegalStateException(paramDescription + " is undetermined");
		}
		if (paramValue == null) {
			return null;
		}
		if (!paramType.isInstance(paramValue)) {
			throw new IllegalStateException(paramDescription + " is not of expected type " + paramType.getSimpleName());
		}
		return paramType.cast(paramValue);
	}

	/**
	 * Returns the caller of the executables. This can be one of the following values:
	 * <ul>
	 *     <li>
	 *         {@code null} if {@link #executables} are constructors or a static methods
	 *     </li>
	 *     <li>
	 *         {@link InfoProvider#INDETERMINATE_VALUE} if {@code executables} are non-static
	 *         methods, but the instance is not known at this point in time.
	 *     </li>
	 *     <li>
	 *         Something else if {@code executables} are non-static methods and the instance is known.
	 *     </li>
	 * </ul>
	 */
	public Object getCaller() {
		return caller;
	}

	/**
	 * Returns the caller, casted to its expected type {@code callerType}.
	 * @throws IllegalStateException if the caller is undetermined or not of the expected type
	 */
	public <T> T getCaller(Class<T> callerType, String callerDescription) {
		if (caller == InfoProvider.INDETERMINATE_VALUE) {
			throw new IllegalStateException((callerDescription + " is undetermined"));
		}
		if (!callerType.isInstance(caller)) {
			throw new IllegalStateException(callerDescription + " is not of expected type " + callerType.getSimpleName());
		}
		return callerType.cast(caller);
	}
}
