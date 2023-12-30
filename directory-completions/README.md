# Directory Completions
This module extends Zenodot by providing code completions for String literals within `File` constructors, `Paths.get()`, and `URI` constructors. The suggested completions are based on existing files or directories, but you can also specify favorites to suggest.

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
# Table of Contents

- [Registering the Directory Completions Extension](#registering-the-directory-completions-extension)
- [Further Options](#further-options)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Registering the Directory Completions Extension

For creating an `ExpressionParser`, you need to specify `ParserSettings`, which you create via a `ParserSettingsBuilder`. This is also the class where you can register this extension module:

```
DirectoryCompletionExtension.create()
	.completionTargets(
		CompletionTarget.FILE_CREATION,
		CompletionTarget.PATH_CREATION,
		CompletionTarget.PATH_RESOLUTION,
		CompletionTarget.URI_CREATION)
	.configure(parserSettingsBuilder);
```

With the completion targets you specify where to apply code completions:
* `FILE_CREATION`: When selected, then you will get code completions in all `String` parameters of
  * `new File(String)`,
  * `new File(String, String)`, and
  * `new File(File, String)`.
* `PATH_CREATION`: When selected, then you will get code completions for the first and the second `String` parameter of
  * `Paths.get(String, String...)` and
  * `FileSystem.getPath(String, String...)`.
* `PATH_RESOLUTION`: When selected, then you will get code completions for the `String` parameter of
  * `Path.resolve(String)` and
  * `Path.resolveSibling(String)`.
* `URI_CREATION`: When selected, then you will get code completions for
  * `new URI(String)` and `URI.create(String)`,
  * `new URI(String, String, String)` (only for the scheme-specific part),
  * `new URI(String, String, String, String)` (only for the path),
  * `new URI(String, String, String, String, String)` (only for the path), and
  * `new URI(String, String, String, int, String, String, String)` (only for the path).

This extension assume that the path typed so far refers to an existing directory plus a partial name of a child (directory or file) within that directory. The completions suggest existing children within that directory that match the child name typed so far.

*Example:* When requesting code completions for the partial expression `new File("C:\\Windows\\ex` on a Windows machine you will most likely get `explorer.exe` as one of the code completions.

# Further Options

There are two types of options you can specify:

* File system access: All file system operations required by this extension are abstracted by the interfaces `FileDirectoryStructure` and `PathDirectoryStructure`. By default, the instances `FileDirectoryStructure.DEFAULT` and `PathDirectoryStructure.DEFAULT` are used. They implement the methods canonically via Java IO and NIO methods, respectively. In some cases, it may be worth to provide other implementations:
  * Caching: You might encounter too high latency due to frequent file system accesses caused by frequent requests for code completions. To reduce this, you can cache these accesses. By calling `FileDirectoryStructure.cache(FileDirectoryStructure, long)` or `PathDirectoryStructure.cache(PathDirectoryStructure, long)` you can create new instances of these interfaces that delegate to the provided instances, but cache the results of their methods for the specified number of milliseconds.
  * Testing: For our unit tests we use internal, system-independent implementations that allow us to write system-independent tests. Note, however, that mocking file systems that way is not useful in other scenarios because these abstractions are only used by this extension for providing code completions. Zenodot itself does not use them when evaluating expressions.
* Favorites: Even with code completions it might be tedious to navigate to the desired directory or file via parent -> child relations. This is particularly annoying for directories or files you need to navigate to regularly. For such cases you can specify favorites via `DirectoryCompletionExtension.favoritePaths()` and `DirectoryCompletionExtension.favoriteUris()`. While the regular code completion only lets you navigate to the next child level, favorite paths and URIs allow you to navigate directly to the referenced destination (even if it does not exist). Only favorites will be suggested that have the path typed so far as prefix.