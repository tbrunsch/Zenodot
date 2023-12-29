package dd.kms.zenodot.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.api.DirectoryCompletionExtension;
import dd.kms.zenodot.api.directories.FileDirectoryStructure;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.api.settings.extensions.CompletionProvider;
import dd.kms.zenodot.api.settings.extensions.ParserExtension;
import dd.kms.zenodot.api.settings.extensions.ParserExtensionBuilder;
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

public class DirectoryCompletionExtensionImpl implements DirectoryCompletionExtension
{
	private FileDirectoryStructure	fileDirectoryStructure	= FileDirectoryStructure.DEFAULT;
	private PathDirectoryStructure	pathDirectoryStructure	= PathDirectoryStructure.DEFAULT;
	private Set<CompletionTarget>	completionTargets		= ImmutableSet.of();
	private List<String>			favoritePaths			= ImmutableList.of();
	private List<URI>				favoriteUris			= ImmutableList.of();

	@Override
	public DirectoryCompletionExtension fileDirectoryStructure(FileDirectoryStructure fileDirectoryStructure) {
		this.fileDirectoryStructure = fileDirectoryStructure;
		return this;
	}

	@Override
	public DirectoryCompletionExtension pathDirectoryStructure(PathDirectoryStructure pathDirectoryStructure) {
		this.pathDirectoryStructure = pathDirectoryStructure;
		return this;
	}

	@Override
	public DirectoryCompletionExtension completionTargets(CompletionTarget... completionTargets) {
		this.completionTargets = ImmutableSet.copyOf(completionTargets);
		return this;
	}

	@Override
	public DirectoryCompletionExtension favoritePaths(List<String> favoritePaths) {
		this.favoritePaths = ImmutableList.copyOf(favoritePaths);
		return this;
	}

	@Override
	public DirectoryCompletionExtension favoriteUris(List<URI> favoriteUris) {
		this.favoriteUris = ImmutableList.copyOf(favoriteUris);
		return this;
	}

	@Override
	public ParserSettingsBuilder configure(ParserSettingsBuilder parserSettingsBuilder) {
		ParserExtensionBuilder parserExtensionBuilder = ParserExtensionBuilder.create();
		for (CompletionTarget completionTarget : completionTargets) {
			configure(parserExtensionBuilder, completionTarget);
		}
		ParserExtension parserExtension = parserExtensionBuilder.build();
		parserSettingsBuilder.setParserExtension(DirectoryCompletionExtension.EXTENSION_NAME, parserExtension);
		return parserSettingsBuilder;
	}

	private void configure(ParserExtensionBuilder parserExtensionBuilder, CompletionTarget completionTarget) {
		switch (completionTarget) {
			case FILE_CREATION:
				configureFileConstructors(parserExtensionBuilder);
				break;
			case PATH_CREATION:
				configurePathCreation(parserExtensionBuilder);
				break;
			case PATH_RESOLUTION:
				configurePathResolution(parserExtensionBuilder);
				break;
			case URI_CREATION:
				configureUriCreation(parserExtensionBuilder);
				break;
			default:
				throw new IllegalArgumentException("Unsupported completion target: " + completionTarget);
		}
	}

	private void configureFileConstructors(ParserExtensionBuilder parserExtensionBuilder) {
		try {
			configureSimpleFileConstructor(parserExtensionBuilder);
			configureFileConstructorRelativeToParentPath(parserExtensionBuilder);
			configureFileConstructorRelativeToParentFile(parserExtensionBuilder);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	private void configureSimpleFileConstructor(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
		Executable fileConstructor = File.class.getConstructor(String.class);
		CompletionProvider completionProvider = new AbsoluteFileDirectoryCompletionProvider(fileDirectoryStructure, favoritePaths);
		parserExtensionBuilder.addStringLiteralCompletionProvider(fileConstructor, 0, completionProvider);
	}

	private void configureFileConstructorRelativeToParentPath(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
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
		parserExtensionBuilder.addStringLiteralCompletionProvider(fileConstructor, 0, completionProviderParameter0);
		parserExtensionBuilder.addStringLiteralCompletionProvider(fileConstructor, 1, completionProviderParameter1);
	}

	private void configureFileConstructorRelativeToParentFile(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
		Executable fileConstructor = File.class.getConstructor(File.class, String.class);
		CompletionProvider completionProviderParameter1 = new AbstractRelativeFileDirectoryCompletionProvider(fileDirectoryStructure, favoritePaths) {
			@Override
			@Nullable
			protected File getRootFile(CallerContext callerContext) {
				return callerContext.getParameter(0, File.class, "parent file");
			}
		};
		parserExtensionBuilder.addStringLiteralCompletionProvider(fileConstructor, 1, completionProviderParameter1);
	}

	private void configurePathCreation(ParserExtensionBuilder parserExtensionBuilder) {
		try {
			configurePathCreationViaPaths(parserExtensionBuilder);
			configurePathCreationViaFileSystem(parserExtensionBuilder);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	private void configurePathCreationViaPaths(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
		Executable pathsGet = Paths.class.getMethod("get", String.class, String[].class);
		CompletionProvider completionProviderParameter0 = new AbsolutePathDirectoryCompletionProvider(pathDirectoryStructure, favoritePaths, callerContext -> pathDirectoryStructure.getDefaultFileSystem());
		CompletionProvider completionProviderParameter1 = new AbstractRelativePathDirectoryCompletionProvider(pathDirectoryStructure, favoritePaths, callerContext -> pathDirectoryStructure.getDefaultFileSystem()) {
			@Override
			protected Path getRootFile(CallerContext callerContext) throws IOException {
				String rootFilePath = callerContext.getParameter(0, String.class, "parent path");
				return pathDirectoryStructure.getFile(FileSystems.getDefault(), rootFilePath);
			}
		};
		parserExtensionBuilder.addStringLiteralCompletionProvider(pathsGet, 0, completionProviderParameter0);
		parserExtensionBuilder.addStringLiteralCompletionProvider(pathsGet, 1, completionProviderParameter1);
	}

	private void configurePathCreationViaFileSystem(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
		Executable fileSystemGetPath = FileSystem.class.getMethod("getPath", String.class, String[].class);
		CompletionProvider completionProviderParameter0 = new AbsolutePathDirectoryCompletionProvider(pathDirectoryStructure, favoritePaths, AbstractPathDirectoryCompletionProvider::getCallerFileSystem);
		CompletionProvider completionProviderParameter1 = new AbstractRelativePathDirectoryCompletionProvider(pathDirectoryStructure, favoritePaths, AbstractPathDirectoryCompletionProvider::getCallerFileSystem) {
			@Override
			protected Path getRootFile(CallerContext callerContext) throws IOException {
				String rootFilePath = callerContext.getParameter(0, String.class, "parent path");
				return pathDirectoryStructure.getFile(getFileSystem(callerContext), rootFilePath);
			}
		};
		parserExtensionBuilder.addStringLiteralCompletionProvider(fileSystemGetPath, 0, completionProviderParameter0);
		parserExtensionBuilder.addStringLiteralCompletionProvider(fileSystemGetPath, 1, completionProviderParameter1);
	}

	private void configurePathResolution(ParserExtensionBuilder parserExtensionBuilder) {
		try {
			configurePathChildResolution(parserExtensionBuilder);
			configurePathSiblingResolution(parserExtensionBuilder);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	private void configurePathChildResolution(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
		Executable pathResolve = Path.class.getMethod("resolve", String.class);
		CompletionProvider completionProviderParameter0 = new AbstractRelativePathDirectoryCompletionProvider(pathDirectoryStructure, favoritePaths, AbstractPathDirectoryCompletionProvider::getCallerFileSystem) {
			@Override
			protected Path getRootFile(CallerContext callerContext) {
				return callerContext.getCaller(Path.class, "parent path");
			}
		};
		parserExtensionBuilder.addStringLiteralCompletionProvider(pathResolve, 0, completionProviderParameter0);
	}

	private void configurePathSiblingResolution(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
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
		parserExtensionBuilder.addStringLiteralCompletionProvider(pathResolveSibling, 0, completionProviderParameter0);
	}

	private void configureUriCreation(ParserExtensionBuilder parserExtensionBuilder) {
		try {
			configureUriCreation1String(parserExtensionBuilder);
			configureUriCreation3Strings(parserExtensionBuilder);
			configureUriCreation4Strings(parserExtensionBuilder);
			configureUriCreation5Strings(parserExtensionBuilder);
			configureUriCreation6Strings1Int(parserExtensionBuilder);
			configureUriCreationFactory(parserExtensionBuilder);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		}
	}

	private void configureUriCreation1String(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
		Executable constructor = URI.class.getConstructor(String.class);
		CompletionProvider completionProviderParameter0 = new AbstractUriDirectoryCompletionProvider(pathDirectoryStructure, favoriteUris, URI::toString, true) {
			@Nullable
			@Override
			protected URI doGetParent(@Nullable String parentPath, CallerContext callerContext) throws Exception {
				if (parentPath == null) {
					return null;
				}
				return new URI(parentPath);
			}
		};
		parserExtensionBuilder.addStringLiteralCompletionProvider(constructor, 0, completionProviderParameter0);
	}

	private void configureUriCreation3Strings(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
		Executable constructor = URI.class.getConstructor(String.class, String.class, String.class);
		CompletionProvider completionProviderParameter1 = new AbstractUriDirectoryCompletionProvider(pathDirectoryStructure, favoriteUris, URI::getSchemeSpecificPart, false) {
			@Override
			protected URI doGetParent(@Nullable String parentPath, CallerContext callerContext) throws Exception {
				String scheme = callerContext.getParameter(0, String.class, "scheme");
				return new URI(scheme, parentPath, null);
			}
		};
		parserExtensionBuilder.addStringLiteralCompletionProvider(constructor, 1, completionProviderParameter1);
	}

	private void configureUriCreation4Strings(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
		Executable constructor = URI.class.getConstructor(String.class, String.class, String.class, String.class);
		CompletionProvider completionProviderParameter2 = new AbstractUriDirectoryCompletionProvider(pathDirectoryStructure, favoriteUris, URI::getPath, false) {
			@Override
			protected URI doGetParent(@Nullable String parentPath, CallerContext callerContext) throws Exception {
				String scheme = callerContext.getParameter(0, String.class, "scheme");
				String host = callerContext.getParameter(1, String.class, "host");
				return new URI(scheme, host, parentPath, null);
			}
		};
		parserExtensionBuilder.addStringLiteralCompletionProvider(constructor, 2, completionProviderParameter2);
	}

	private void configureUriCreation5Strings(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
		Executable constructor = URI.class.getConstructor(String.class, String.class, String.class, String.class, String.class);
		CompletionProvider completionProviderParameter2 = new AbstractUriDirectoryCompletionProvider(pathDirectoryStructure, favoriteUris, URI::getPath, false) {
			@Override
			protected URI doGetParent(@Nullable String parentPath, CallerContext callerContext) throws Exception {
				String scheme = callerContext.getParameter(0, String.class, "scheme");
				String host = callerContext.getParameter(1, String.class, "host");
				return new URI(scheme, host, parentPath, null);
			}
		};
		parserExtensionBuilder.addStringLiteralCompletionProvider(constructor, 2, completionProviderParameter2);
	}

	private void configureUriCreation6Strings1Int(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
		Executable constructor = URI.class.getConstructor(String.class, String.class, String.class, int.class, String.class, String.class, String.class);
		CompletionProvider completionProviderParameter4 = new AbstractUriDirectoryCompletionProvider(pathDirectoryStructure, favoriteUris, URI::getPath, false) {
			@Override
			protected URI doGetParent(@Nullable String parentPath, CallerContext callerContext) throws Exception {
				String scheme = callerContext.getParameter(0, String.class, "scheme");
				String userInfo = callerContext.getParameter(1, String.class, "user info");
				String host = callerContext.getParameter(2, String.class, "host");
				int port = callerContext.getParameter(3, Integer.class, "port");
				return new URI(scheme, userInfo, host, port, parentPath, null, null);
			}
		};
		parserExtensionBuilder.addStringLiteralCompletionProvider(constructor, 4, completionProviderParameter4);
	}

	private void configureUriCreationFactory(ParserExtensionBuilder parserExtensionBuilder) throws NoSuchMethodException {
		Executable createUri = URI.class.getMethod("create", String.class);
		CompletionProvider completionProviderParameter0 = new AbstractUriDirectoryCompletionProvider(pathDirectoryStructure, favoriteUris, URI::toString, true) {
			@Nullable
			@Override
			protected URI doGetParent(@Nullable String parentPath, CallerContext callerContext) {
				if (parentPath == null) {
					return null;
				}
				return URI.create(parentPath);
			}
		};
		parserExtensionBuilder.addStringLiteralCompletionProvider(createUri, 0, completionProviderParameter0);
	}
}
