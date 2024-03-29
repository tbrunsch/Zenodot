package dd.kms.zenodot.impl.completionproviders;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.directories.FileDirectoryStructure;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.CompletionMode;
import dd.kms.zenodot.framework.parsers.CallerContext;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AbstractFileDirectoryCompletionProvider extends AbstractDirectoryCompletionProvider<File>
{
	protected final FileDirectoryStructure	fileDirectoryStructure;
	private final List<File>				favoriteFiles;

	protected AbstractFileDirectoryCompletionProvider(FileDirectoryStructure fileDirectoryStructure, List<String> favoritePaths) {
		this.fileDirectoryStructure = fileDirectoryStructure;

		ImmutableList.Builder<File> favoriteFilesBuilder = ImmutableList.builder();
		for (String favoritePath : favoritePaths) {
			try {
				File favoriteFile = fileDirectoryStructure.getFile(favoritePath);
				favoriteFilesBuilder.add(favoriteFile);
			} catch (Exception ignored) {
				/* simply skip this file */
			}
		}
		this.favoriteFiles = favoriteFilesBuilder.build();
	}

	@Override
	protected List<CodeCompletion> doGetCodeCompletions(NullableOptional<File> parent, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, CallerContext callerContext) {
		if (!parent.isPresent()) {
			return Collections.emptyList();
		}
		File actualParent = parent.get();
		List<CodeCompletion> codeCompletions = new ArrayList<>();
		try {
			List<File> children = actualParent != null
				? fileDirectoryStructure.getChildren(actualParent)
				: fileDirectoryStructure.getRootDirectories();
			for (File child : children) {
				String childName = child.getName();
				if (childName.trim().isEmpty() && child.getParent() == null) {
					// happens for Windows drives
					String path = child.getPath();
					int lastCharPos = path.length() - 1;
					childName = lastCharPos >= 0 && isSeparator(path.charAt(lastCharPos))
						? path.substring(0, lastCharPos)
						: path;
				}
				CodeCompletion codeCompletion = createCodeCompletion(childName, childCompletionInfo, completionMode);
				codeCompletions.add(codeCompletion);
			}
		} catch (Exception ignored) {
			/* fallthrough to return the code completions gathered so far */
		}
		return codeCompletions;
	}

	@Override
	protected List<CodeCompletion> doGetFavoriteCompletions(NullableOptional<File> parent, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, CallerContext callerContext) {
		if (!parent.isPresent()) {
			return Collections.emptyList();
		}
		File actualParent = parent.get();
		List<CodeCompletion> favoriteCompletions = new ArrayList<>();
		try {
			for (File favoriteFile : favoriteFiles) {
				String relativeFavoriteName = getChildName(favoriteFile, actualParent);
				if (relativeFavoriteName != null) {
					CodeCompletion codeCompletion = createCodeCompletion(escapeBackslashs(relativeFavoriteName), childCompletionInfo, completionMode, relativeFavoriteName);
					favoriteCompletions.add(codeCompletion);
				}
			}
		} catch (Exception ignored) {
			/* fallthrough to return the code completions gathered so far */
		}
		return favoriteCompletions;
	}

	@Nullable
	private String getChildName(File child, File parent) {
		String childPath = child.toString();
		if (parent == null) {
			return childPath;
		}
		String parentPath = parent.toString();
		if (childPath.startsWith(parentPath) && !Objects.equals(childPath, parentPath)) {
			int beginIndex = parentPath.length();
			if (isSeparator(childPath.charAt(beginIndex))) {
				beginIndex++;
			}
			return childPath.substring(beginIndex);
		}
		return null;
	}
}
