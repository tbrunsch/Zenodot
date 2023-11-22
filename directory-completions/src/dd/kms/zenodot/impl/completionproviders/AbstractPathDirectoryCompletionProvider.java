package dd.kms.zenodot.impl.completionproviders;

import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.settings.CompletionMode;
import dd.kms.zenodot.framework.parsers.CallerContext;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractPathDirectoryCompletionProvider extends AbstractDirectoryCompletionProvider<Path>
{
	protected final PathDirectoryStructure				pathDirectoryStructure;
	private final Function<CallerContext, FileSystem>	fileSystemProvider;

	public AbstractPathDirectoryCompletionProvider(PathDirectoryStructure pathDirectoryStructure, Function<CallerContext, FileSystem> fileSystemProvider) {
		this.pathDirectoryStructure = pathDirectoryStructure;
		this.fileSystemProvider = fileSystemProvider;
	}

	@Override
	protected List<? extends CodeCompletion> doGetCodeCompletions(@Nullable Path parent, ChildCompletionInfo childCompletionInfo, CompletionMode completionMode, CallerContext callerContext) {
		List<CodeCompletion> codeCompletions = new ArrayList<>();
		try {
			List<Path> children = parent != null
				? pathDirectoryStructure.getChildren(parent)
				: pathDirectoryStructure.getRootDirectories(getFileSystem(callerContext));
			try {
				for (Path child : children) {
					Path childFileName = child.getFileName();
					String childName = childFileName != null
						? childFileName.toString()
						: child.toString();			// for root directories
					CodeCompletion codeCompletion = createCodeCompletion(childName, childCompletionInfo, completionMode);
					codeCompletions.add(codeCompletion);
				}
			} finally {
				if (children instanceof Closeable) {
					((Closeable) children).close();
				}
			}
		} catch (Exception ignored) {
			/* fallthrough to return the code completions gathered so far*/
		}
		return codeCompletions;
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
