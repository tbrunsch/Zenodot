package dd.kms.zenodot.impl.completionproviders;

import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.framework.parsers.CallerContext;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class AbsolutePathDirectoryCompletionProvider extends AbstractPathDirectoryCompletionProvider
{
	public AbsolutePathDirectoryCompletionProvider(PathDirectoryStructure pathDirectoryStructure, List<String> favoritePaths, Function<CallerContext, FileSystem> fileSystemProvider) {
		super(pathDirectoryStructure, favoritePaths, fileSystemProvider);
	}

	@Nullable
	@Override
	protected Path doGetParent(@Nullable String parentPath, CallerContext callerContext) throws IOException {
		return parentPath != null ? pathDirectoryStructure.getFile(getFileSystem(callerContext), parentPath) : null;
	}
}
