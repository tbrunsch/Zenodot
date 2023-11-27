package dd.kms.zenodot.impl.completionproviders;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.CompletionMode;
import dd.kms.zenodot.framework.parsers.CallerContext;

import javax.annotation.Nullable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractPathDirectoryCompletionProvider extends AbstractDirectoryCompletionProvider<Path>
{
	protected final PathDirectoryStructure				pathDirectoryStructure;
	private final Function<CallerContext, FileSystem>	fileSystemProvider;
	private final List<Path>							favoriteFiles;

	public AbstractPathDirectoryCompletionProvider(PathDirectoryStructure pathDirectoryStructure, List<String> favoritePaths, Function<CallerContext, FileSystem> fileSystemProvider) {
		this.pathDirectoryStructure = pathDirectoryStructure;
		this.fileSystemProvider = fileSystemProvider;

		ImmutableList.Builder<Path> favoriteFilesBuilder = ImmutableList.builder();
		for (String favoritePath : favoritePaths) {
			try {
				Path favoriteFile = Paths.get(favoritePath);
				favoriteFilesBuilder.add(favoriteFile);
			} catch (Exception ignored) {
				/* simply skip this file */
			}
		}
		this.favoriteFiles = favoriteFilesBuilder.build();
	}

	@Override
	protected List<CodeCompletion> doGetCodeCompletions(NullableOptional<Path> parent, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, CallerContext callerContext) {
		if (!parent.isPresent()) {
			return Collections.emptyList();
		}
		Path actualParent = parent.get();
		List<CodeCompletion> codeCompletions = new ArrayList<>();
		try {
			List<Path> children = actualParent != null
				? pathDirectoryStructure.getChildren(actualParent)
				: pathDirectoryStructure.getRootDirectories(getFileSystem(callerContext));
			for (Path child : children) {
				Path childFileName = child.getFileName();
				String childName = childFileName != null
					? childFileName.toString()
					: child.toString();			// for root directories
				CodeCompletion codeCompletion = createCodeCompletion(childName, childCompletionInfo, completionMode);
				codeCompletions.add(codeCompletion);
			}
		} catch (Exception ignored) {
			/* fallthrough to return the code completions gathered so far */
		}
		return codeCompletions;
	}

	@Override
	protected List<CodeCompletion> doGetFavoriteCompletions(NullableOptional<Path> parent, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, CallerContext callerContext) {
		if (!parent.isPresent()) {
			return Collections.emptyList();
		}
		Path actualParent = parent.get();
		if (actualParent != null && !Objects.equals(actualParent.getFileSystem(), FileSystems.getDefault())) {
			return Collections.emptyList();
		}
		List<CodeCompletion> favoriteCompletions = new ArrayList<>();
		try {
			for (Path favoriteFile : favoriteFiles) {
				String relativeFavoriteName = getChildName(favoriteFile, actualParent);
				if (relativeFavoriteName != null) {
					CodeCompletion codeCompletion = createCodeCompletion(relativeFavoriteName, childCompletionInfo, completionMode);
					favoriteCompletions.add(codeCompletion);
				}
			}
		} catch (Exception ignored) {
			/* fallthrough to return the code completions gathered so far */
		}
		return favoriteCompletions;
	}

	@Nullable
	private String getChildName(Path child, Path parent) {
		String childPath = child.toString();
		if (parent == null) {
			return childPath;
		}
		String parentPath = parent.toString();
		return childPath.startsWith(parentPath) && !Objects.equals(childPath, parentPath)
			? childPath.substring(parentPath.length())
			: null;
	}

	protected FileSystem getFileSystem(CallerContext callerContext) {
		return fileSystemProvider.apply(callerContext);
	}

	public static FileSystem getDefaultFileSystem(CallerContext callerContext) {
		return FileSystems.getDefault();
	}

	public static FileSystem getCallerFileSystem(CallerContext callerContext) {
		return callerContext.getCaller(FileSystem.class, "file system");
	}
}
