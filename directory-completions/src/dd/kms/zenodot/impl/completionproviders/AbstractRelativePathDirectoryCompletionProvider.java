package dd.kms.zenodot.impl.completionproviders;

import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.framework.parsers.CallerContext;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.function.Function;

public abstract class AbstractRelativePathDirectoryCompletionProvider extends AbstractPathDirectoryCompletionProvider
{
	public AbstractRelativePathDirectoryCompletionProvider(PathDirectoryStructure pathDirectoryStructure, Function<CallerContext, FileSystem> fileSystemProvider) {
		super(pathDirectoryStructure, fileSystemProvider);
	}

	protected abstract Path getRootFile(CallerContext callerContext) throws IOException;

	@Override
	@Nullable
	protected Path doGetParent(@Nullable String parentPath, CallerContext callerContext) throws IOException {
		Path rootFile = getRootFile(callerContext);
		return parentPath != null ? pathDirectoryStructure.resolve(rootFile, parentPath) : rootFile;
	}
}
