package dd.kms.zenodot.impl.utils;

import com.google.common.collect.*;
import com.google.common.reflect.ClassPath;
import dd.kms.zenodot.api.common.multistringmatching.MultiStringMatcher;
import dd.kms.zenodot.impl.wrappers.ClassInfo;
import dd.kms.zenodot.impl.wrappers.InfoProvider;

import java.io.IOException;
import java.util.*;

/**
 * Provides utility methods for class names.
 */
public class ClassUtils
{
	private static final Set<String>					TOP_LEVEL_CLASS_NAMES;
	private static final SetMultimap<String, ClassInfo> TOP_LEVEL_CLASS_INFOS_BY_PACKAGE_NAMES;
	private static final Set<String>					PACKAGE_NAMES;
	private static final MultiStringMatcher<ClassInfo>	TOP_LEVEL_CLASSES_BY_UNQUALIFIED_NAMES;

	static {
		Set<String> topLevelClassNames = new HashSet<>();
		SetMultimap<String, ClassInfo> topLevelClassInfosByPackageNames = HashMultimap.create();
		TOP_LEVEL_CLASSES_BY_UNQUALIFIED_NAMES = new MultiStringMatcher<>();
		try {
			ClassPath classPath = ClassPath.from(ClassLoader.getSystemClassLoader());
			for (ClassPath.ClassInfo topLevelClass : classPath.getTopLevelClasses()) {
				String qualifiedClassName = topLevelClass.getName();
				topLevelClassNames.add(qualifiedClassName);
				ClassInfo classInfo = InfoProvider.createClassInfoUnchecked(qualifiedClassName);
				String packageName = ClassUtils.getParentPath(qualifiedClassName);
				if (packageName != null) {
					topLevelClassInfosByPackageNames.put(packageName, classInfo);
				}
				String unqualifiedName = ClassUtils.getLeafOfPath(qualifiedClassName);
				TOP_LEVEL_CLASSES_BY_UNQUALIFIED_NAMES.put(unqualifiedName, classInfo);
			}
		} catch (IOException ignored) {
			/* nothing we can do here */
		}
		TOP_LEVEL_CLASS_NAMES = ImmutableSet.copyOf(topLevelClassNames);
		TOP_LEVEL_CLASS_INFOS_BY_PACKAGE_NAMES = ImmutableSetMultimap.copyOf(topLevelClassInfosByPackageNames);
		TOP_LEVEL_CLASSES_BY_UNQUALIFIED_NAMES.makeImmutable();

		Set<String> packageNames = new LinkedHashSet<>();
		for (String mainPackageName : TOP_LEVEL_CLASS_INFOS_BY_PACKAGE_NAMES.keySet()) {
			for (String packageName = mainPackageName; packageName != null; packageName = ClassUtils.getParentPath(packageName)) {
				packageNames.add(packageName);
			}
		}
		PACKAGE_NAMES = ImmutableSet.copyOf(packageNames);
	}

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
	 * @throws ClassNotFoundException if the method determines that the class does not exist.
	 * {@param loadClassForExistenceCheck} is {@code true}, then the class will be loaded
	 * for the existence check. Otherwise, only obvious reasons for the non-existence will
	 * lead to this exception.
	 */
	public static String normalizeClassName(String qualifiedClassName, boolean loadClassForExistenceCheck) throws ClassNotFoundException {
		String normalizedClassName = normalizeClassName(qualifiedClassName);
		if (loadClassForExistenceCheck) {
			/* Class<?> ignored = */ Class.forName(qualifiedClassName);
		}
		return normalizedClassName;
	}

	private static String normalizeClassName(String qualifiedClassName) throws ClassNotFoundException {
		int dollarPos = qualifiedClassName.indexOf('$');
		if (dollarPos >= 0) {
			String innerClassNamePart = qualifiedClassName.substring(dollarPos);
			if (innerClassNamePart.indexOf('.') >= 0) {
				// inner class name part contains '$' and '.'
				throw new ClassNotFoundException("Invalid class name '" + qualifiedClassName + "'");
			}
			String topLevelClassName = qualifiedClassName.substring(0, dollarPos);
			if (!TOP_LEVEL_CLASS_NAMES.contains(topLevelClassName)) {
				throw new ClassNotFoundException("Unknown top level class name '" + topLevelClassName + "' of '" + qualifiedClassName + "'");
			}
			// qualifiedClassName is already normalized
			return qualifiedClassName;
		}

		// qualified class name is a top level class name
		if (TOP_LEVEL_CLASS_NAMES.contains(qualifiedClassName)) {
			return qualifiedClassName;
		}

		int nextDotPos = -1;
		while (true) {
			nextDotPos = qualifiedClassName.indexOf('.', nextDotPos + 1);
			if (nextDotPos < 0) {
				throw new ClassNotFoundException("Unknown class '" + qualifiedClassName + "'");
			}
			String topLevelClassName = qualifiedClassName.substring(0, nextDotPos);
			if (TOP_LEVEL_CLASS_NAMES.contains(topLevelClassName)) {
				String remainderClassName = qualifiedClassName.substring(nextDotPos).replace('.', '$');
				return topLevelClassName + remainderClassName;
			}
		}
	}

	/**
	 * Returns the regular class name of a fully qualified class name. See {@link #normalizeClassName(String, boolean)}
	 * for an explanation of regular and normalized class names.
	 */
	public static String getRegularClassName(String qualifiedClassName) {
		return qualifiedClassName.replace('$', '.');
	}

	public static MultiStringMatcher<ClassInfo> getClassesByUnqualifiedNames(List<String> innerClassNames) {
		if (innerClassNames.isEmpty()) {
			return TOP_LEVEL_CLASSES_BY_UNQUALIFIED_NAMES;
		}
		MultiStringMatcher<ClassInfo> classesByUnqualifiedNames = new MultiStringMatcher<>(TOP_LEVEL_CLASSES_BY_UNQUALIFIED_NAMES);
		for (String innerClassName : innerClassNames) {
			String simpleClassName = ClassUtils.getLeafOfPath(innerClassName);
			if (!Character.isDigit(simpleClassName.charAt(0))) {
				classesByUnqualifiedNames.put(simpleClassName, new ClassInfo(innerClassName));
			}
		}
		classesByUnqualifiedNames.makeImmutable();
		return classesByUnqualifiedNames;
	}

	public static Collection<String> getPackageNames() {
		return PACKAGE_NAMES;
	}

	public static boolean packageExists(String packageName) {
		return PACKAGE_NAMES.contains(packageName);
	}

	public static Set<ClassInfo> getTopLevelClassesInPackages(Collection<String> packageNames) {
		Set<ClassInfo> classes = new HashSet<>();
		for (String packageName : Iterables.filter(packageNames, Objects::nonNull)) {
			Set<ClassInfo> classInfos = TOP_LEVEL_CLASS_INFOS_BY_PACKAGE_NAMES.get(packageName);
			classes.addAll(classInfos);
		}
		return classes;
	}

}
