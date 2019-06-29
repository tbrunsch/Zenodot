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
import dd.kms.zenodot.utils.wrappers.*;

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

	public Class<?> getImportedClass(String className) {
		return Stream.of(
				PRIMITIVE_CLASSES_BY_NAME.get(className),
				getClassImportedViaClassName(className),
				getClassImportedViaPackage(className)
			).filter(Objects::nonNull)
			.findFirst().orElse(null);
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
		return getImportedPackages().stream()
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

	private Set<PackageInfo> getImportedPackages() {
		Set<PackageInfo> importedPackages = new LinkedHashSet<>();
		if (thisClass != null) {
			Package pack = thisClass.getPackage();
			// package is null for, e.g., arrays
			if (pack != null) {
				importedPackages.add(InfoProvider.createPackageInfo(pack.getName()));
			}
		}
		importedPackages.add(InfoProvider.createPackageInfo("java.lang"));
		importedPackages.addAll(imports.getImportedPackages());
		return importedPackages;
	}

	private static Set<ClassInfo> getTopLevelClassesInPackages(Collection<PackageInfo> packages) {
		Set<ClassInfo> classes = new HashSet<>();
		for (PackageInfo pack : Iterables.filter(packages, Objects::nonNull)) {
			for (ClassPath.ClassInfo classInfo : CLASS_PATH.getTopLevelClasses(pack.getPackageName())) {
				classes.add(InfoProvider.createClassInfoUnchecked(classInfo.getName()));
			}
		}
		return classes;
	}

	/*
	 * Package Suggestions
	 */
	public CompletionSuggestions suggestPackages(int insertionBegin, int insertionEnd, String packagePrefix) {
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

		Map<CompletionSuggestion, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
			suggestedPackageNames,
			packageName -> new CompletionSuggestionPackage(packageName, insertionBegin, insertionEnd),
			ratePackageFunc(subpackagePrefix)
		);

		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}
	private static StringMatch ratePackageByName(String packageName, String expectedName) {
		int lastDotIndex = packageName.lastIndexOf('.');
		String subpackageName = packageName.substring(lastDotIndex + 1);
		return MatchRatings.rateStringMatch(subpackageName, expectedName);
	}

	private static Function<String, MatchRating> ratePackageFunc(String expectedName) {
		return packageName -> MatchRatings.create(ratePackageByName(packageName, expectedName), TypeMatch.NONE, AccessMatch.IGNORED);
	}

	/*
	 * Class Suggestions
	 */
	public static CompletionSuggestions suggestQualifiedClasses(int insertionBegin, int insertionEnd, String classPrefixWithPackage) {
		String packageName = ClassUtils.getParentPath(classPrefixWithPackage);
		if (packageName == null) {
			// class is not fully qualified, so no match
			return CompletionSuggestions.none(insertionEnd);
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
			newSuggestedClasses.add(clazz);
		}
		String classPrefix = ClassUtils.getLeafOfPath(classPrefixWithPackage);

		Map<CompletionSuggestion, MatchRating> ratedSuggestions = ParseUtils.createRatedSuggestions(
			newSuggestedClasses,
			classInfo -> new CompletionSuggestionClass(classInfo, insertionBegin, insertionEnd),
			rateClassFunc(classPrefix)
		);

		return new CompletionSuggestions(insertionBegin, ratedSuggestions);
	}

	public CompletionSuggestions suggestImportedClasses(int insertionBegin, int insertionEnd, String classPrefix) {
		ImmutableMap.Builder<CompletionSuggestion, MatchRating> suggestionBuilder = ImmutableMap.builder();

		Set<ClassInfo> importedClasses = getImportedClasses();
		Set<ClassInfo> topLevelClassesInPackages = getTopLevelClassesInPackages(getImportedPackages());
		Set<ClassInfo> additionalTopLevelClassesInPackage = Sets.difference(topLevelClassesInPackages, importedClasses);

		suggestionBuilder.putAll(suggestUnqualifiedClasses(insertionBegin, insertionEnd, classPrefix, importedClasses));
		suggestionBuilder.putAll(suggestUnqualifiedClasses(insertionBegin, insertionEnd, classPrefix, additionalTopLevelClassesInPackage));

		return new CompletionSuggestions(insertionBegin, suggestionBuilder.build());
	}

	private static Map<CompletionSuggestion, MatchRating> suggestUnqualifiedClasses(int insertionBegin, int insertionEnd, String classPrefix, Set<ClassInfo> classes) {
		if (ClassUtils.lastIndexOfPathSeparator(classPrefix) >= 0) {
			// class is fully qualified, so no match
			return ImmutableMap.of();
		}
		return ParseUtils.createRatedSuggestions(
			classes,
			classInfo -> new CompletionSuggestionClass(classInfo, insertionBegin, insertionEnd),
			rateClassFunc(classPrefix)
		);
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

	private static StringMatch rateClassByName(ClassInfo classInfo, String expectedSimpleClassName) {
		return MatchRatings.rateStringMatch(classInfo.getUnqualifiedName(), expectedSimpleClassName);
	}

	private static Function<ClassInfo, MatchRating> rateClassFunc(String simpleClassName) {
		return classInfo -> MatchRatings.create(rateClassByName(classInfo, simpleClassName), TypeMatch.NONE, AccessMatch.IGNORED);
	}

	public static String getClassDisplayText(ClassInfo classInfo) {
		return classInfo.getUnqualifiedName();
	}
}
