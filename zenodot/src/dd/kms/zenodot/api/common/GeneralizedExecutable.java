package dd.kms.zenodot.api.common;

import javax.annotation.Nullable;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;

/**
 * This interface is an abstraction of the class {@link Executable} that can also represent extension methods
 * (a concept known in C#, but not available in Java). Except for that single case, it simply wraps an
 * {@code Executable} and delegates all methods to it. For the sake of simplicity, this interface does not
 * contain all methods provided by {@code Executable}. If you need more methods and the
 * {@code GeneralizedExecutable} is backed up by an {@code Executable}, then you can obtain that
 * {@code Executable} by calling {@link #getWrappedExecutable()}. As of now, this method will only return
 * {@code null} if the instance represents an extension method.
 */
public interface GeneralizedExecutable extends Member
{
	/**
	 * @return The underlying {@code Executable}, if available, or {@code null} otherwise. Note
	 * that there is currently only one case in which there is no underlying executable: If the
	 * {@code GeneralizedExecutable} represents an extension method.
	 */
	@Nullable
	Executable getWrappedExecutable();

	/**
	 * @see Executable#getDeclaringClass()
	 */
	Class<?> getDeclaringClass();

	/**
	 * @see Executable#getName()
	 */
	String getName();

	/**
	 * @see Executable#getModifiers()
	 */
	int getModifiers();

	/**
	 * @see Executable#getParameterTypes()
	 */
	Class<?>[] getParameterTypes();

	/**
	 * @see Executable#getParameterCount()
	 */
	int getParameterCount();

	/**
	 * @see Executable#isVarArgs()
	 */
	boolean isVarArgs();

	/**
	 * If the {@code GeneralizedExecutable} represents a constructor, then
	 * {@link java.lang.reflect.Constructor#getDeclaringClass()} is called on the wrapped constructor.
	 *
	 * @see java.lang.reflect.Method#getReturnType().
	 */
	Class<?> getReturnType();

	/**
	 * If the {@code GeneralizedExecutable} represents a constructor, then
	 * {@link java.lang.reflect.Constructor#newInstance(Object...)} is called on the wrapped constructor,
	 * ignoring the first parameter {@code obj}.
	 *
	 * @see java.lang.reflect.Method#invoke(Object, Object...)
	 *
	 * @apiNote If this {@code GeneralizedExecutable} wraps an {@link Executable}, then
	 * {@code setAccessible(true)} will be automatically called before invoking the {@code Executable}.
	 */
	Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException,
		InvocationTargetException;
}
