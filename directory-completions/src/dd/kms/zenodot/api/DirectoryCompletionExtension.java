package dd.kms.zenodot.api;

import dd.kms.zenodot.api.directories.FileDirectoryStructure;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;
import dd.kms.zenodot.impl.DirectoryCompletionExtensionImpl;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

public interface DirectoryCompletionExtension
{
	String	EXTENSION_NAME	= "Directory Completions";

	static DirectoryCompletionExtension create() {
		return new DirectoryCompletionExtensionImpl();
	}

	/**
	 * By default, all completions for {@link File} constructors (see {@link CompletionTarget#FILE_CREATION})
	 * are based on file system queries (behavior of {@link FileDirectoryStructure#DEFAULT}). However, they
	 * are abstracted by the interface {@link FileDirectoryStructure}. That way, you can influence how the
	 * completions are generated: You can, e.g., cache these accesses
	 * (see {@link FileDirectoryStructure#cache(FileDirectoryStructure, long)}).
	 */
	DirectoryCompletionExtension fileDirectoryStructure(FileDirectoryStructure fileDirectoryStructure);

	/**
	 * By default, all completions for {@link Path}-related operations (see {@link CompletionTarget#PATH_CREATION},
	 * {@link CompletionTarget#PATH_RESOLUTION}, and {@link CompletionTarget#URI_CREATION}) are based on file
	 * system queries (behavior of {@link PathDirectoryStructure#DEFAULT}). However, they are abstracted by the
	 * interface {@link PathDirectoryStructure}. That way, you can influence how the completions are generated:
	 * You can, e.g., cache these accesses (see {@link PathDirectoryStructure#cache(PathDirectoryStructure, long)}).
	 */
	DirectoryCompletionExtension pathDirectoryStructure(PathDirectoryStructure pathDirectoryStructure);

	/**
	 * Specify for which methods to provide code completions.
	 */
	DirectoryCompletionExtension completionTargets(CompletionTarget... completionTargets);

	/**
	 * By default, only children of the specified path will be suggested for completions. Favorite paths
	 * (referencing files or directories on the default file system) that extend the specified path will
	 * always be suggested, even if they are no direct children.
	 */
	DirectoryCompletionExtension favoritePaths(List<String> favoritePaths);

	/**
	 * By default, only children of the specified path will be suggested for completions. Favorite {@link URI}s
	 * that extend the specified path will always be suggested, even if they are no direct children.
	 */
	DirectoryCompletionExtension favoriteUris(List<URI> favoriteUris);

	ParserSettingsBuilder configure(ParserSettingsBuilder parserSettingsBuilder);

	enum CompletionTarget
	{
		/**
		 * Refers to the constructors
		 * <ul>
		 *     <li>{@link java.io.File#File(String)},</li>
		 *     <li>{@link java.io.File#File(String, String)}, and</li>
		 *     <li>{@link java.io.File#File(File, String)}.</li>
		 * </ul>
		 */
		FILE_CREATION,

		/**
		 * Refers to the factory methods
		 * <ul>
		 *     <li>{@link java.nio.file.Paths#get(String, String...)} and.</li>
		 *     <li>{@link java.nio.file.FileSystem#getPath(String, String...)}.</li>
		 * </ul>
		 */
		PATH_CREATION,

		/**
		 * Refers to the methods
		 * <ul>
		 *     <li>{@link java.nio.file.Path#resolve(String)} and</li>
		 *     <li>{@link java.nio.file.Path#resolveSibling(String)}.</li>
		 * </ul>
		 */
		PATH_RESOLUTION,

		/**
		 * Refers to the constructors
		 * <ul>
		 *     <li>{@link java.net.URI#URI(String)},</li>
		 *     <li>{@link java.net.URI#URI(String, String, String)},</li>
		 *     <li>{@link java.net.URI#URI(String, String, String, String)},</li>
		 *     <li>{@link java.net.URI#URI(String, String, String, String, String)}, and</li>
		 *     <li>{@link java.net.URI#URI(String, String, String, int, String, String, String)}</li>
		 * </ul>
		 * and the factory method
		 * <ul>
		 *     <li>{@link java.net.URI#create(String)}.</li>
		 * </ul>
		 */
		URI_CREATION
	}
}
