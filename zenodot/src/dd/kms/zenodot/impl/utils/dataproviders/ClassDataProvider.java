package dd.kms.zenodot.impl.utils.dataproviders;

import com.google.common.collect.*;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.ClassPath;
import dd.kms.zenodot.api.common.multistringmatching.MultiStringMatcher;
import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.matching.StringMatch;
import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.Imports;
import dd.kms.zenodot.api.wrappers.*;
import dd.kms.zenodot.impl.matching.MatchRatings;
import dd.kms.zenodot.impl.result.CodeCompletions;
import dd.kms.zenodot.impl.result.codecompletions.CodeCompletionFactory;
import dd.kms.zenodot.impl.utils.ClassUtils;
import dd.kms.zenodot.impl.utils.ParseUtils;
import dd.kms.zenodot.impl.utils.ParserToolbox;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for providing information about classes and packages
 */
public class ClassDataProvider
{
	private static final Map<String, Class<?>>			PRIMITIVE_CLASSES_BY_NAME				= Primitives.allPrimitiveTypes().stream()
		.collect(Collectors.toMap(
					Class::getName,
					clazz -> clazz
				)
		);
	private static final List<ClassInfo>				PRIMITIVE_CLASS_INFOS					= PRIMITIVE_CLASSES_BY_NAME.keySet().stream().map(InfoProvider::createClassInfoUnchecked).collect(Collectors.toList());

	private static final SetMultimap<String, ClassInfo> TOP_LEVEL_CLASS_INFOS_BY_PACKAGE_NAMES;
	private static final Set<String>					PACKAGE_NAMES;
	private static final MultiStringMatcher<ClassInfo>	TOP_LEVEL_CLASSES_BY_UNQUALIFIED_NAMES;

	static {
		TOP_LEVEL_CLASS_INFOS_BY_PACKAGE_NAMES = HashMultimap.create();
		TOP_LEVEL_CLASSES_BY_UNQUALIFIED_NAMES = new MultiStringMatcher<>();
		try {
			ClassPath classPath = ClassPath.from(ClassLoader.getSystemClassLoader());
			for (ClassPath.ClassInfo topLevelClass : classPath.getTopLevelClasses()) {
				String qualifiedClassName = topLevelClass.getName();
				ClassInfo classInfo = InfoProvider.createClassInfoUnchecked(qualifiedClassName);
				String packageName = ClassUtils.getParentPath(classInfo.getNormalizedName());
				TOP_LEVEL_CLASS_INFOS_BY_PACKAGE_NAMES.put(packageName, classInfo);
				String unqualifiedName = ClassUtils.getLeafOfPath(qualifiedClassName);
				TOP_LEVEL_CLASSES_BY_UNQUALIFIED_NAMES.put(unqualifiedName, classInfo);
			}
		} catch (IOException e) {
		}

		Set<String> packageNames = new LinkedHashSet<>();
		for (String mainPackageName : TOP_LEVEL_CLASS_INFOS_BY_PACKAGE_NAMES.keySet()) {
			for (String packageName = mainPackageName; packageName != null; packageName = ClassUtils.getParentPath(packageName)) {
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
		this.thisClass = parserToolbox.getObjectInfoProvider().getType(thisInfo);
	}

	public boolean packageExists(String packageName) {
		return PACKAGE_NAMES.contains(packageName);
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
			Set<ClassInfo> classInfos = TOP_LEVEL_CLASS_INFOS_BY_PACKAGE_NAMES.get(pack.getPackageName());
			classes.addAll(classInfos);
		}
		return classes;
	}

	/*
	 * Package Completions
	 */
	public CodeCompletions completePackage(int insertionBegin, int insertionEnd, String packagePrefix) {
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

		List<CodeCompletion> codeCompletions = ParseUtils.createCodeCompletions(
			suggestedPackageNames,
			packageName -> CodeCompletionFactory.packageCompletion(packageName, insertionBegin, insertionEnd, ratePackage(packageName, subpackagePrefix))
		);

		return new CodeCompletions(codeCompletions);
	}
	private static StringMatch ratePackageByName(String packageName, String expectedName) {
		int lastDotIndex = packageName.lastIndexOf('.');
		String subpackageName = packageName.substring(lastDotIndex + 1);
		return MatchRatings.rateStringMatch(expectedName, subpackageName);
	}

	private static MatchRating ratePackage(String packageName, String expectedName) {
		return MatchRatings.create(ratePackageByName(packageName, expectedName), TypeMatch.NONE, false);
	}

	/*
	 * Class Completions
	 */
	public static CodeCompletions completeQualifiedClasses(int insertionBegin, int insertionEnd, String classPrefixWithPackage) {
		String packageName = ClassUtils.getParentPath(classPrefixWithPackage);
		if (packageName == null) {
			// class is not fully qualified, so no match
			return CodeCompletions.NONE;
		}
		Set<ClassInfo> suggestedClasses = TOP_LEVEL_CLASS_INFOS_BY_PACKAGE_NAMES.get(packageName);
		String classPrefix = ClassUtils.getLeafOfPath(classPrefixWithPackage);

		List<CodeCompletion> codeCompletions = ParseUtils.createCodeCompletions(
			suggestedClasses,
			classInfo -> CodeCompletionFactory.classCompletion(classInfo, insertionBegin, insertionEnd, false, rateClass(classInfo, classPrefix))
		);

		return new CodeCompletions(codeCompletions);
	}

	public CodeCompletions completeClassName(int insertionBegin, int insertionEnd, String classPrefix, boolean considerAllClasses) {
		ImmutableList.Builder<CodeCompletion> completionsBuilder = ImmutableList.builder();

		Set<ClassInfo> importedClasses = getImportedClasses();
		Set<ClassInfo> topLevelClassesInPackages = getTopLevelClassesInPackages(getImportedPackages());
		Set<ClassInfo> additionalTopLevelClassesInPackage = Sets.difference(topLevelClassesInPackages, importedClasses);

		completionsBuilder.addAll(completeUnqualifiedClass(insertionBegin, insertionEnd, classPrefix, importedClasses));
		completionsBuilder.addAll(completeUnqualifiedClass(insertionBegin, insertionEnd, classPrefix, additionalTopLevelClassesInPackage));

		if (!classPrefix.isEmpty() && considerAllClasses) {
			// We only search all top level classes if the class prefix is not empty to avoid generating code completions for all top level classes
			Set<ClassInfo> classesToIgnoreForQualifiedClasses = Sets.union(importedClasses, additionalTopLevelClassesInPackage);
			completionsBuilder.addAll(completeUnqualifiedClassNameToQualifiedClass(insertionBegin, insertionEnd, classPrefix, classesToIgnoreForQualifiedClasses));
		}

		return new CodeCompletions(completionsBuilder.build());
	}

	private static List<CodeCompletion> completeUnqualifiedClass(int insertionBegin, int insertionEnd, String classPrefix, Set<ClassInfo> classes) {
		if (ClassUtils.lastIndexOfPathSeparator(classPrefix) >= 0) {
			// class is fully qualified, so no match
			return ImmutableList.of();
		}
		return ParseUtils.createCodeCompletions(
			classes,
			classInfo -> CodeCompletionFactory.classCompletion(classInfo, insertionBegin, insertionEnd, false, rateClass(classInfo, classPrefix))
		);
	}

	private static List<CodeCompletion> completeUnqualifiedClassNameToQualifiedClass(int insertionBegin, int insertionEnd, String classPrefix, Set<ClassInfo> classesToIgnore) {
		ImmutableList.Builder<CodeCompletion> completionsBuilder = ImmutableList.builder();
		Set<ClassInfo> classInfos = TOP_LEVEL_CLASSES_BY_UNQUALIFIED_NAMES.search(classPrefix, 100);
		Set<ClassInfo> classInfosToConsider = Sets.difference(classInfos, classesToIgnore);
		for (ClassInfo classInfo : classInfosToConsider) {
			String unqualifiedName = classInfo.getUnqualifiedName();
			StringMatch stringMatch = MatchRatings.rateStringMatch(classPrefix, unqualifiedName);
			if (stringMatch != StringMatch.NONE) {
				MatchRating rating = MatchRatings.create(stringMatch, TypeMatch.NONE, false);
				CodeCompletion codeCompletion = CodeCompletionFactory.classCompletion(classInfo, insertionBegin, insertionEnd, true, rating);
				completionsBuilder.add(codeCompletion);
			}
		}
		return completionsBuilder.build();
	}

	public CodeCompletions completeInnerClass(String expectedName, Class<?> contextClass, int insertionBegin, int insertionEnd) {
		List<ClassInfo> classesToConsider = Arrays.stream(contextClass.getDeclaredClasses())
			.map(clazz -> InfoProvider.createClassInfoUnchecked(clazz.getName()))
			.collect(Collectors.toList());
		List<CodeCompletion> codeCompletions = ParseUtils.createCodeCompletions(
			classesToConsider,
			classInfo -> CodeCompletionFactory.classCompletion(classInfo, insertionBegin, insertionEnd, false, rateClass(classInfo, expectedName))
		);
		return new CodeCompletions(codeCompletions);
	}

	private static StringMatch rateClassByName(ClassInfo classInfo, String expectedSimpleClassName) {
		return MatchRatings.rateStringMatch(expectedSimpleClassName, classInfo.getUnqualifiedName());
	}

	private static MatchRating rateClass(ClassInfo classInfo, String simpleClassName) {
		return MatchRatings.create(rateClassByName(classInfo, simpleClassName), TypeMatch.NONE, false);
	}
}
