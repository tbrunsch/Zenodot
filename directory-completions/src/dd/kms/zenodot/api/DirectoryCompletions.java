package dd.kms.zenodot.api;

import dd.kms.zenodot.api.directories.FileDirectoryStructure;
import dd.kms.zenodot.api.directories.PathDirectoryStructure;
import dd.kms.zenodot.api.settings.ParserSettingsBuilder;

import java.io.File;

public interface DirectoryCompletions
{
	static DirectoryCompletions create() {
		return new dd.kms.zenodot.impl.DirectoryCompletionsImpl();
	}

	DirectoryCompletions fileDirectoryStructure(FileDirectoryStructure fileDirectoryStructure);
	DirectoryCompletions pathDirectoryStructure(PathDirectoryStructure pathDirectoryStructure);
	DirectoryCompletions completionTargets(CompletionTarget... completionTargets);

	void configure(ParserSettingsBuilder parserSettingsBuilder);

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