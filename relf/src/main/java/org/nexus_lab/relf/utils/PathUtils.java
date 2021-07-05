package org.nexus_lab.relf.utils;

import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;

/**
 * @author Ruipeng Zhang
 */
public final class PathUtils {
    private PathUtils() {
    }

    /**
     * Normalizes a path, removing double and single dot path steps.
     *
     * @param path the filename to normalize, null returns null
     * @return the normalized filename, or null if invalid. Null bytes inside string will be removed
     * @see FilenameUtils#normalize(String)
     */
    public static String normalize(String path) {
        return FilenameUtils.normalize(path);
    }

    /**
     * Gets the base name, minus the full path, from a full filename.
     *
     * @param path the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists. Null bytes
     * inside string will
     * be removed
     * @see FilenameUtils#getName(String)
     */
    public static String getName(String path) {
        return FilenameUtils.getName(path);
    }

    /**
     * Gets the base name, minus the full path and extension, from a full filename.
     *
     * @param path the filename to query, null returns null
     * @return the name of the file without the path, or an empty string if none exists. Null bytes
     * inside string will
     * be removed
     * @see FilenameUtils#getBaseName(String)
     */
    public static String getBaseName(String path) {
        return FilenameUtils.getBaseName(path);
    }

    /**
     * Gets the full path from a full filename, which is the prefix + path.
     *
     * @param path the filename to query, null returns null
     * @return the path of the file, an empty string if none exists, null if invalid
     * @see FilenameUtils#getFullPath(String)
     */
    public static String getPath(String path) {
        return FilenameUtils.getFullPath(path);
    }

    /**
     * @param path an array of path segments.
     * @return the joined path string.
     * @see FilenameUtils#concat(String, String)
     */
    public static String join(String... path) {
        return join(Arrays.asList(path));
    }

    /**
     * @param path an {@link Iterable} of path segments.
     * @return the joined path string.
     * @see FilenameUtils#concat(String, String)
     */
    public static String join(Iterable<String> path) {
        String base = null;
        boolean first = true;
        for (String s : path) {
            if (!first) {
                base = FilenameUtils.concat(base, s);
            } else {
                base = s;
                first = false;
            }
        }
        return base;
    }

    /**
     * Concat the given path segments to a path string, duplicated / will be removed.
     *
     * @param path an array of path segments
     * @return the joined path string
     */
    public static String concat(String... path) {
        return concat(Arrays.asList(path));
    }

    /**
     * Concat the given path segments to a path string, duplicated / will be removed.
     *
     * @param path an {@link Iterable} of path segments
     * @return the joined path string
     */
    public static String concat(Iterable<String> path) {
        StringBuilder builder = new StringBuilder();
        String separator = "";
        for (String s : path) {
            if (s == null) {
                continue;
            }
            builder.append(separator);
            separator = "/";
            builder.append(s);
        }
        return builder.toString().replaceAll("/{2,}", "/");
    }
}
