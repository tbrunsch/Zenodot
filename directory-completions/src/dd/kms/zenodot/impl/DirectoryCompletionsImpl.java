package dd.kms.zenodot.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.api.DirectoryCompletions;
import dd.kms.zenodot.api.directories.FileDirectoryStructure;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.settings.parsers.CompletionProvider;
import dd.kms.zenodot.framework.parsers.CallerContext;
import dd.kms.zenodot.impl.completionproviders.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Executable;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

public class DirectoryCompletionsImpl implements DirectoryCompletions
{
	private FileDirectoryStructure	fileDirectoryStructure	= FileDirectoryStructure.DEFAULT;
	private PathDirectoryStructure	pathDirectoryStructure	= PathDirectoryStructure.DEFAULT;
	private Set<CompletionTarget>	completionTargets		= ImmutableSet.of();
	private List<String>			favoritePaths			= ImmutableList.of();
	private List<URI>				favoriteURIs			= ImmutableList.of();

	@Override
	public DirectoryCompletions fileDirectoryStructure(FileDirectoryStructure fileDirectoryStructure) {
		this.fileDirectoryStructure = fileDirectoryStructure;
		return this;
	}

	@Override
	public DirectoryCompletions pathDirectoryStructure(PathDirectoryStructure pathDirectoryStructure) {
		this.pathDirectoryStructure = pathDirectoryStructure;
		return this;
	}

	@Override
	public DirectoryCompletions completionTargets(CompletionTarget... completionTargets) {
		this.completionTargets = ImmutableSet.copyOf(completionTargets);
		return this;
	}

	@Override
	public DirectoryCompletions favoritePaths(List<String> favoritePaths) {
		this.favoritePaths = ImmutableList.copyOf(favoritePaths);
		return this;
	}

	@Override
	public DirectoryCompletions favoriteURIs(List<URI> favoriteURIs) {
		this.favoriteURIs = ImmutableList.copyOf(favoriteURIs);
		return this;
	}

	@Override
	public void configure(ParserSettingsBuilder parserSettingsBuilder) {
		for (CompletionTarget completionTarget : completionTargets) {
			configure(parserSettingsBuilder, completionTarget);
		}
	}

	private void configure(ParserSettingsBuilder parserSettingsBuilder, CompletionTarget completionTarget) {
		switch (completionTarget) {
			case FILE_CREATION:
				configureFileConstructors(parserSettingsBuilder);
				break;
			case PATH_CREATION:
				configurePathCreation(parserSettingsBuilder);
				break;
			case PATH_RESOLUTION:
				configurePathResolution(parserSettingsBuilder);
				break;
			case URI_CREATION:
				configureURICreation(parserSettingsBuilder);
				break;
			default:
				throw new IllegalArgumentException("Unsupported completion target: " + completionTarget);
		}
	}

	private void configureFileConstructors(ParserSettingsBuilder parserSettingsBuilder) {
		try {
			configureSimpleFileConstructor(parserSettingsBuilder);
			configureFileConstructorRelativeToParentPath(parserSettingsBuilder);
			configureFileConstructorRelativeToParentFile(parserSettingsBuilder);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	private void configureSimpleFileConstructor(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable fileConstructor = File.class.getConstructor(String.class);
		CompletionProvider completionProvider = new AbsoluteFileDirectoryCompletionProvider(fileDirectoryStructure, favoritePaths);
		parserSettingsBuilder.stringLiteralCompletionProvider(fileConstructor, 0, completionProvider);
	}

	private void configureFileConstructorRelativeToParentPath(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable fileConstructor = File.class.getConstructor(String.class, String.class);
		CompletionProvider completionProviderParameter0 = new AbsoluteFileDirectoryCompletionProvider(fileDirectoryStructure, favoritePaths);
		CompletionProvider completionProviderParameter1 = new AbstractRelativeFileDirectoryCompletionProvider(fileDirectoryStructure, favoritePaths) {
			@Override
			@Nullable
			protected File getRootFile(CallerContext callerContext) throws IOException {
				String rootFilePath = callerContext.getParameter(0, String.class, "parent path");
				return fileDirectoryStructure.getFile(rootFilePath);
			}
		};
		parserSettingsBuilder.stringLiteralCompletionProvider(fileConstructor, 0, completionProviderParameter0);
		parserSettingsBuilder.stringLiteralCompletionProvider(fileConstructor, 1, completionProviderParameter1);
	}

	private void configureFileConstructorRelativeToParentFile(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable fileConstructor = File.class.getConstructor(File.class, String.class);
		CompletionProvider completionProviderParameter1 = new AbstractRelativeFileDirectoryCompletionProvider(fileDirectoryStructure, favoritePaths) {
			@Override
			@Nullable
			protected File getRootFile(CallerContext callerContext) {
				return callerContext.getParameter(0, File.class, "parent file");
			}
		};
		parserSettingsBuilder.stringLiteralCompletionProvider(fileConstructor, 1, completionProviderParameter1);
	}

	private void configurePathCreation(ParserSettingsBuilder parserSettingsBuilder) {
		try {
			configurePathCreationViaPaths(parserSettingsBuilder);
			configurePathCreationViaFileSystem(parserSettingsBuilder);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	private void configurePathCreationViaPaths(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable pathsGet = Paths.class.getMethod("get", String.class, String[].class);
		CompletionProvider completionProviderParameter0 = new AbsolutePathDirectoryCompletionProvider(pathDirectoryStructure, favoritePaths, AbstractPathDirectoryCompletionProvider::getDefaultFileSystem);
		CompletionProvider completionProviderParameter1 = new AbstractRelativePathDirectoryCompletionProvider(pathDirectoryStructure, favoritePaths, AbstractPathDirectoryCompletionProvider::getDefaultFileSystem) {
			@Override
			protected Path getRootFile(CallerContext callerContext) throws IOException {
				String rootFilePath = callerContext.getParameter(0, String.class, "parent path");
				return pathDirectoryStructure.getFile(FileSystems.getDefault(), rootFilePath);
			}
		};
		parserSettingsBuilder.stringLiteralCompletionProvider(pathsGet, 0, completionProviderParameter0);
		parserSettingsBuilder.stringLiteralCompletionProvider(pathsGet, 1, completionProviderParameter1);
	}

	private void configurePathCreationViaFileSystem(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable fileSystemGetPath = FileSystem.class.getMethod("getPath", String.class, String[].class);
		CompletionProvider completionProviderParameter0 = new AbsolutePathDirectoryCompletionProvider(pathDirectoryStructure, favoritePaths, AbstractPathDirectoryCompletionProvider::getCallerFileSystem);
		CompletionProvider completionProviderParameter1 = new AbstractRelativePathDirectoryCompletionProvider(pathDirectoryStructure, favoritePaths, AbstractPathDirectoryCompletionProvider::getCallerFileSystem) {
			@Override
			protected Path getRootFile(CallerContext callerContext) throws IOException {
				String rootFilePath = callerContext.getParameter(0, String.class, "parent path");
				return pathDirectoryStructure.getFile(getFileSystem(callerContext), rootFilePath);
			}
		};
		parserSettingsBuilder.stringLiteralCompletionProvider(fileSystemGetPath, 0, completionProviderParameter0);
		parserSettingsBuilder.stringLiteralCompletionProvider(fileSystemGetPath, 1, completionProviderParameter1);
	}

	private void configurePathResolution(ParserSettingsBuilder parserSettingsBuilder) {
		try {
			configurePathChildResolution(parserSettingsBuilder);
			configurePathSiblingResolution(parserSettingsBuilder);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	private void configurePathChildResolution(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable pathResolve = Path.class.getMethod("resolve", String.class);
		CompletionProvider completionProviderParameter0 = new AbstractRelativePathDirectoryCompletionProvider(pathDirectoryStructure, favoritePaths, AbstractPathDirectoryCompletionProvider::getCallerFileSystem) {
			@Override
			protected Path getRootFile(CallerContext callerContext) {
				return callerContext.getCaller(Path.class, "parent path");
			}
		};
		parserSettingsBuilder.stringLiteralCompletionProvider(pathResolve, 0, completionProviderParameter0);
	}

	private void configurePathSiblingResolution(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable pathResolveSibling = Path.class.getMethod("resolveSibling", String.class);
		CompletionProvider completionProviderParameter0 = new AbstractRelativePathDirectoryCompletionProvider(pathDirectoryStructure, favoritePaths, AbstractPathDirectoryCompletionProvider::getCallerFileSystem) {
			@Override
			protected Path getRootFile(CallerContext callerContext) throws IOException {
				Path siblingPath = callerContext.getCaller(Path.class, "sibling path");
				Path parentPath = pathDirectoryStructure.getParent(siblingPath);
				if (parentPath == null) {
					throw new IllegalStateException("Cannot resolve sibling of root path");
				}
				return parentPath;
			}
		};
		parserSettingsBuilder.stringLiteralCompletionProvider(pathResolveSibling, 0, completionProviderParameter0);
	}

	private void configureURICreation(ParserSettingsBuilder parserSettingsBuilder) {
		try {
			configureURICreation1String(parserSettingsBuilder);
			configureURICreation3Strings(parserSettingsBuilder);
			configureURICreation4Strings(parserSettingsBuilder);
			configureURICreation5Strings(parserSettingsBuilder);
			configureURICreation6Strings1Int(parserSettingsBuilder);
			configureURICreationFactory(parserSettingsBuilder);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	private void configureURICreation1String(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable constructor = URI.class.getConstructor(String.class);
		CompletionProvider completionProviderParameter0 = new AbstractURIDirectoryCompletionProvider(pathDirectoryStructure, favoriteURIs, URI::toString, true) {
			@Nullable
			@Override
			protected URI doGetParent(@Nullable String parentPath, CallerContext callerContext) throws Exception {
				if (parentPath == null) {
					return null;
				}
				return new URI(parentPath);
			}
		};
		parserSettingsBuilder.stringLiteralCompletionProvider(constructor, 0, completionProviderParameter0);
	}

	private void configureURICreation3Strings(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable constructor = URI.class.getConstructor(String.class, String.class, String.class);
		CompletionProvider completionProviderParameter1 = new AbstractURIDirectoryCompletionProvider(pathDirectoryStructure, favoriteURIs, URI::getSchemeSpecificPart, false) {
			@Override
			protected URI doGetParent(@Nullable String parentPath, CallerContext callerContext) throws Exception {
				String scheme = callerContext.getParameter(0, String.class, "scheme");
				return new URI(scheme, parentPath, null);
			}
		};
		parserSettingsBuilder.stringLiteralCompletionProvider(constructor, 1, completionProviderParameter1);
	}

	private void configureURICreation4Strings(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable constructor = URI.class.getConstructor(String.class, String.class, String.class, String.class);
		CompletionProvider completionProviderParameter2 = new AbstractURIDirectoryCompletionProvider(pathDirectoryStructure, favoriteURIs, URI::getPath, false) {
			@Override
			protected URI doGetParent(@Nullable String parentPath, CallerContext callerContext) throws Exception {
				String scheme = callerContext.getParameter(0, String.class, "scheme");
				String host = callerContext.getParameter(1, String.class, "host");
				return new URI(scheme, host, parentPath, null);
			}
		};
		parserSettingsBuilder.stringLiteralCompletionProvider(constructor, 2, completionProviderParameter2);
	}

	private void configureURICreation5Strings(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable constructor = URI.class.getConstructor(String.class, String.class, String.class, String.class, String.class);
		CompletionProvider completionProviderParameter2 = new AbstractURIDirectoryCompletionProvider(pathDirectoryStructure, favoriteURIs, URI::getPath, false) {
			@Override
			protected URI doGetParent(@Nullable String parentPath, CallerContext callerContext) throws Exception {
				String scheme = callerContext.getParameter(0, String.class, "scheme");
				String host = callerContext.getParameter(1, String.class, "host");
				return new URI(scheme, host, parentPath, null);
			}
		};
		parserSettingsBuilder.stringLiteralCompletionProvider(constructor, 2, completionProviderParameter2);
	}

	private void configureURICreation6Strings1Int(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable constructor = URI.class.getConstructor(String.class, String.class, String.class, int.class, String.class, String.class, String.class);
		CompletionProvider completionProviderParameter4 = new AbstractURIDirectoryCompletionProvider(pathDirectoryStructure, favoriteURIs, URI::getPath, false) {
			@Override
			protected URI doGetParent(@Nullable String parentPath, CallerContext callerContext) throws Exception {
				String scheme = callerContext.getParameter(0, String.class, "scheme");
				String userInfo = callerContext.getParameter(1, String.class, "user info");
				String host = callerContext.getParameter(2, String.class, "host");
				int port = callerContext.getParameter(3, Integer.class, "port");
				return new URI(scheme, userInfo, host, port, parentPath, null, null);
			}
		};
		parserSettingsBuilder.stringLiteralCompletionProvider(constructor, 4, completionProviderParameter4);
	}

	private void configureURICreationFactory(ParserSettingsBuilder parserSettingsBuilder) throws NoSuchMethodException {
		Executable createURI = URI.class.getMethod("create", String.class);
		CompletionProvider completionProviderParameter0 = new AbstractURIDirectoryCompletionProvider(pathDirectoryStructure, favoriteURIs, URI::toString, true) {
			@Nullable
			@Override
			protected URI doGetParent(@Nullable String parentPath, CallerContext callerContext) {
				if (parentPath == null) {
					return null;
				}
				return URI.create(parentPath);
			}
		};
		parserSettingsBuilder.stringLiteralCompletionProvider(createURI, 0, completionProviderParameter0);
	}
}
