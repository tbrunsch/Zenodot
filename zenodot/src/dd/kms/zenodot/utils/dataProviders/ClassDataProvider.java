package dd.kms.zenodot.utils.dataProviders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.ClassPath;
import dd.kms.zenodot.matching.*;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.CompletionSuggestions;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionClass;
import dd.kms.zenodot.result.completionSuggestions.CompletionSuggestionPackage;
import dd.kms.zenodot.settings.Imports;
import dd.kms.zenodot.utils.ClassUtils;
import dd.kms.zenodot.utils.ParseUtils;
import dd.kms.zenodot.utils.ParserToolbox;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for providing information about classes and packages
 */
public class ClassDataProvider
{
	private static final Map<String, Class<?>>	PRIMITIVE_CLASSES_BY_NAME	= Primitives.allPrimitiveTypes().stream()
		.collect(Collectors.toMap(
					Class::getName,
					clazz -> clazz
				)
		);
	private static final List<ClassInfo>		PRIMITIVE_CLASS_INFOS		= PRIMITIVE_CLASSES_BY_NAME.keySet().stream().map(InfoProvider::createClassInfoUnchecked).collect(Collectors.toList());

	private static final ClassPath				CLASS_PATH;
	private static final Set<String>			TOP_LEVEL_CLASS_NAMES;
	private static final Set<String>			PACKAGE_NAMES;

	static {
		ClassPath classPath;
		Set<String> topLevelClassNames;
		try {
			classPath = ClassPath.from(ClassLoader.getSystemClassLoader());
			topLevelClassNames = classPath.getTopLevelClasses()
				.stream()
				.map(ClassPath.ClassInfo::getName)
				.collect(Collectors.toSet());
		} catch (IOException e) {
			classPath = null;
			topLevelClassNames = ImmutableSet.of();
		}
		CLASS_PATH = classPath;

		TOP_LEVEL_CLASS_NAMES = topLevelClassNames;

		Set<String> packageNames = new LinkedHashSet<>();
		for (String className : TOP_LEVEL_CLASS_NAMES) {
			for (String packageName = ClassUtils.getParentPath(className); packageName != null; packageName = ClassUtils.getParentPath(packageName)) {
				packageNames.add(packageName);
			}
		}
		PACKAGE_NAMES = ImmutableSet.copyOf(packageNames);
	}

	private final Imports	imports;
	private final Class<?>	thisClass;

	public ClassDataProvider(ParserToolbox parserToolbox) {
		this.imports = parserToolbox.getSettings().getImports();
		ObjectInfo thisInfo = parserToolbox.getThisInfo();
		TypeInfo thisType = parserToolbox.getObjectInfoProvider().getType(thisInfo);
		this.thisClass = thisType.getRawType();
	}

	public boolean packageExists(String packageName) {
		String packagePrefix = packageName + ".";
		return TOP_LEVEL_CLASS_NAMES.stream().anyMatch(name -> name.startsWith(packagePrefix));
	}

	public Class<?> detectClass(String className) {
		return Stream.of(
				PRIMITIVE_CLASSES_BY_NAME.get(className),
				getClassImportedViaClassName(className),
				getClassImportedViaPackage(className),
				ClassUtils.getClassUnchecked(className)
			).filter(Objects::nonNull)
			.findFirst().orElse(null);
	}

	public CompletionSuggestions suggestClassesAndPackages(int insertionBegin, int insertionEnd, String classOrPackagePrefix) {
		ImmutableMap.Builder<CompletionSuggestion, MatchRating> suggestionBuilder = ImmutableMap.builder();

		Set<ClassInfo> importedClasses = getImportedClasses();
		Set<String> importedPackageNames = getImportedPackageNames();
		Set<ClassInfo> topLevelClassesInPackages = getTopLevelClassesInPackages(importedPackageNames);

		Set<ClassInfo> suggestedClasses = new HashSet<>();

		suggestionBuilder.putAll(suggestUnqualifiedClasses(classOrPackagePrefix, insertionBegin, insertionEnd, importedClasses, suggestedClasses));
		suggestionBuilder.putAll(suggestUnqualifiedClasses(classOrPackagePrefix, insertionBegin, insertionBegin, topLevelClassesInPackages, suggestedClasses));
		suggestionBuilder.putAll(suggestQualifiedClasses(classOrPackagePrefix, insertionBegin, insertionEnd, suggestedClasses));
		suggestionBuilder.putAll(suggestPackages(classOrPackagePrefix, insertionBegin, insertionEnd));

		return new CompletionSuggestions(insertionBegin, suggestionBuilder.build());
	}

	public CompletionSuggestions suggestInnerClasses(String expectedName, Class<?> contextClass, int insertionBegin, int insertionEnd) {
		List<ClassInfo> classesToConsider = Arrays.stream(contextClass.getDeclaredClasses())
			.map(clazz -> InfoProvider.createClassInfoUnchecked(clazz.getName()))
			.collect(Collectors.toList());
		Map<CompletionSuggestion, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
			classesToConsider,
			classInfo -> new CompletionSuggestionClass(classInfo, insertionBegin, insertionEnd),
			rateClassFunc(expectedName)
		);
		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	private Class<?> getClassImportedViaClassName(String className) {
		for (ClassInfo importedClass : getImportedClasses()) {
			String unqualifiedName = importedClass.getUnqualifiedName();
			if (className.equals(unqualifiedName) || className.startsWith(unqualifiedName + ".")) {
				// Replace simpleName by fully qualified imported name and replace '.' by '$' when separating inner classes
				String fullyQualifiedClassName = importedClass.getNormalizedName()
						+ className.substring(unqualifiedName.length()).replace('.', '$');
				return ClassUtils.getClassUnchecked(fullyQualifiedClassName);
			}
		}
		return null;
	}

	private Class<?> getClassImportedViaPackage(String className) {
		return getImportedPackageNames().stream()
				.map(packageName -> packageName + "." + className)
				.map(ClassUtils::getClassUnchecked)
				.filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	private Set<ClassInfo> getImportedClasses() {
		Set<ClassInfo> importedClasses = new LinkedHashSet<>();
		importedClasses.addAll(PRIMITIVE_CLASS_INFOS);
		if (thisClass != null) {
			importedClasses.add(InfoProvider.createClassInfoUnchecked(thisClass.getName()));
		}
		importedClasses.addAll(imports.getImportedClasses());
		return importedClasses;
	}

	private Set<String> getImportedPackageNames() {
		Set<String> importedPackageNames = new LinkedHashSet<>();
		if (thisClass != null) {
			importedPackageNames.add(thisClass.getPackage().getName());
		}
		importedPackageNames.add("java.lang");
		importedPackageNames.addAll(imports.getImportedPackageNames());
		return importedPackageNames;
	}

	private static Set<ClassInfo> getTopLevelClassesInPackages(Collection<String> packageNames) {
		Set<ClassInfo> classes = new HashSet<>();
		for (String packageName : Iterables.filter(packageNames, Objects::nonNull)) {
			for (ClassPath.ClassInfo classInfo : CLASS_PATH.getTopLevelClasses(packageName)) {
				classes.add(InfoProvider.createClassInfoUnchecked(classInfo.getName()));
			}
		}
		return classes;
	}

	private static Map<CompletionSuggestion, MatchRating> suggestUnqualifiedClasses(String classPrefix, int insertionBegin, int insertionEnd, Set<ClassInfo> classes, Set<ClassInfo> suggestedClasses) {
		if (ClassUtils.lastIndexOfPathSeparator(classPrefix) >= 0) {
			// class is fully qualified, so no match
			return ImmutableMap.of();
		}
		Sets.SetView<ClassInfo> newSuggestedClasses = Sets.difference(classes, suggestedClasses);
		classes.addAll(newSuggestedClasses);
		return ParseUtils.createRatedSuggestions(
				newSuggestedClasses,
				classInfo -> new CompletionSuggestionClass(classInfo, insertionBegin, insertionEnd),
				rateClassFunc(classPrefix)
		);
	}

	private static Map<CompletionSuggestion, MatchRating> suggestQualifiedClasses(String classPrefixWithPackage, int insertionBegin, int insertionEnd, Set<ClassInfo> suggestedClasses) {
		String packageName = ClassUtils.getParentPath(classPrefixWithPackage);
		if (packageName == null) {
			// class is not fully qualified, so no match
			return ImmutableMap.of();
		}
		String prefix = packageName + ".";
		int lastSeparatorIndex = packageName.length();
		List<ClassInfo> newSuggestedClasses = new ArrayList<>();
		for (String className : TOP_LEVEL_CLASS_NAMES) {
			if (ClassUtils.lastIndexOfPathSeparator(className) != lastSeparatorIndex) {
				continue;
			}
			if (!className.startsWith(prefix)) {
				continue;
			}
			ClassInfo clazz = InfoProvider.createClassInfoUnchecked(className);
			if (suggestedClasses.contains(clazz)) {
				continue;
			}
			newSuggestedClasses.add(clazz);
		}
		suggestedClasses.addAll(newSuggestedClasses);
		String classPrefix = ClassUtils.getLeafOfPath(classPrefixWithPackage);
		return ParseUtils.createRatedSuggestions(
				newSuggestedClasses,
				classInfo -> new CompletionSuggestionClass(classInfo, insertionBegin, insertionEnd),
				rateClassFunc(classPrefix)
		);
	}

	private static Map<CompletionSuggestion, MatchRating> suggestPackages(String packagePrefix, int insertionBegin, int insertionEnd) {
		String parentPackage = ClassUtils.getParentPath(packagePrefix);
		int lastSeparatorIndex = ClassUtils.lastIndexOfPathSeparator(packagePrefix);
		List<String> suggestedPackageNames = new ArrayList<>();
		for (String packageName : PACKAGE_NAMES) {
			if (ClassUtils.lastIndexOfPathSeparator(packageName) != lastSeparatorIndex) {
				continue;
			}
			if (parentPackage != null && !packageName.startsWith(parentPackage)) {
				continue;
			}
			suggestedPackageNames.add(packageName);
		}
		String subpackagePrefix = ClassUtils.getLeafOfPath(packagePrefix);
		return ParseUtils.createRatedSuggestions(
				suggestedPackageNames,
				packageName -> new CompletionSuggestionPackage(packageName, insertionBegin, insertionEnd),
				ratePackageFunc(subpackagePrefix)
		);
	}

	/*
	 * Class Suggestions
	 */
	private static StringMatch rateClassByName(ClassInfo classInfo, String expectedSimpleClassName) {
		return MatchRatings.rateStringMatch(classInfo.getUnqualifiedName(), expectedSimpleClassName);
	}

	private static Function<ClassInfo, MatchRating> rateClassFunc(String simpleClassName) {
		return classInfo -> MatchRatings.create(rateClassByName(classInfo, simpleClassName), TypeMatch.NONE, AccessMatch.IGNORED);
	}

	public static String getClassDisplayText(ClassInfo classInfo) {
		return classInfo.getNormalizedName();
	}

	/*
	 * Package Suggestions
	 */
	private static StringMatch ratePackageByName(String packageName, String expectedName) {
		int lastDotIndex = packageName.lastIndexOf('.');
		String subpackageName = packageName.substring(lastDotIndex + 1);
		return MatchRatings.rateStringMatch(subpackageName, expectedName);
	}

	private static Function<String, MatchRating> ratePackageFunc(String expectedName) {
		return packageName -> MatchRatings.create(ratePackageByName(packageName, expectedName), TypeMatch.NONE, AccessMatch.IGNORED);
	}
}
