package dd.kms.zenodot.impl.utils;

/**
 * Provides utility methods for class names.
 */
public class ClassUtils
{
	public static Class<?> getClassUnchecked(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			return null;
		}
	}

	public static int lastIndexOfPathSeparator(String path) {
		return Math.max(path.lastIndexOf('.'), path.lastIndexOf('$'));
	}

	public static String getParentPath(String path) {
		int lastSeparatorIndex = lastIndexOfPathSeparator(path);
		return lastSeparatorIndex < 0 ? null : path.substring(0, lastSeparatorIndex);
	}

	public static String getLeafOfPath(String path) {
		int lastSeparatorIndex = lastIndexOfPathSeparator(path);
		return path.substring(lastSeparatorIndex + 1);
	}

	/**
	 * When referencing classes in the source code, a dot ({@code .}) is used to separate
	 * <ul>
	 *     <li>subpackage names from their parent package names,</li>
	 *     <li>top level class names from package names, and</li>
	 *     <li>inner class names from their parent class names.</li>
	 * </ul>
	 * However, when referencing a class via reflection, inner class names must be separated
	 * from their parent class names with a dollar sign ({@code $}). We call class names in
	 * the former style <b>regular class names</b> and class names in the latter style
	 * <b>normalized class names</b>.
	 *
	 * @throws ClassNotFoundException
	 */
	public static String normalizeClassName(String qualifiedClassName) throws ClassNotFoundException {
		if (ClassUtils.getClassUnchecked(qualifiedClassName) != null) {
			return qualifiedClassName;
		}

		int lastSeparatorPos = -1;
		while (true) {
			int nextDotPos		= qualifiedClassName.indexOf('.', lastSeparatorPos + 1);
			int nextDollarPos	= qualifiedClassName.indexOf('$', lastSeparatorPos + 1);
			int nextSeparatorPos =	nextDotPos < 0		? nextDollarPos :
									nextDollarPos < 0	? nextDotPos
														: Math.min(nextDotPos, nextDollarPos);
			if (nextSeparatorPos < 0) {
				throw new ClassNotFoundException("Unknown class '" + qualifiedClassName + "'");
			}
			Class<?> clazz = ClassUtils.getClassUnchecked(qualifiedClassName.substring(0, nextSeparatorPos));
			if (clazz != null) {
				String topLevelClassName = qualifiedClassName.substring(0, nextSeparatorPos);
				String remainderClassName = qualifiedClassName.substring(nextSeparatorPos).replace('.', '$');
				String normalizedClassName = topLevelClassName + remainderClassName;
				if (Class.forName(normalizedClassName) == null) {
					throw new ClassNotFoundException("Unknown class '" + qualifiedClassName + "'");
				}
				return normalizedClassName;
			}
			lastSeparatorPos = nextSeparatorPos;
		}
	}

	/**
	 * Returns the regular class name of a fully qualified class name. See {@link #normalizeClassName(String)}
	 * for an explanation of regular and normalized class names.
	 */
	public static String getRegularClassName(String qualifiedClassName) {
		return qualifiedClassName.replace('$', '.');
	}
}
