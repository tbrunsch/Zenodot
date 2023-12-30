package dd.kms.zenodot.impl.completionproviders;

import dd.kms.zenodot.api.directories.FileDirectoryStructure;
import dd.kms.zenodot.framework.parsers.CallerContext;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class AbstractRelativeFileDirectoryCompletionProvider extends AbstractFileDirectoryCompletionProvider
{
	public AbstractRelativeFileDirectoryCompletionProvider(FileDirectoryStructure fileDirectoryStructure, List<String> favoritePaths) {
		super(fileDirectoryStructure, favoritePaths);
	}

	@Nullable
	protected abstract File getRootFile(CallerContext callerContext) throws IOException;

	@Override
	@Nullable
	protected File doGetParent(@Nullable String parentPath, CallerContext callerContext) throws IOException {
		File rootFile = getRootFile(callerContext);
		return rootFile != null
			? (parentPath != null ? fileDirectoryStructure.resolve(rootFile, parentPath) : rootFile)
			: (parentPath != null ? new File(parentPath) : null);
	}
}
