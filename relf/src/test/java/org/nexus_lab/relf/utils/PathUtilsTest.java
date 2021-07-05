package org.nexus_lab.relf.utils;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Ruipeng Zhang
 */
public class PathUtilsTest {
    private final static String SEP = "" + File.separatorChar;

    public void unless() {
    }

    /**
     * Copied from {@link org.apache.commons.io.FilenameUtils#normalize(String)} test cases.
     */
    @Test
    public void normalize() {
        assertNull(PathUtils.normalize(null));
        assertNull(PathUtils.normalize(":"));
        assertNull(PathUtils.normalize("1:\\a\\b\\c.txt"));
        assertNull(PathUtils.normalize("1:"));
        assertNull(PathUtils.normalize("1:a"));
        assertNull(PathUtils.normalize("\\\\\\a\\b\\c.txt"));
        assertNull(PathUtils.normalize("\\\\a"));

        assertEquals("a" + SEP + "b" + SEP + "c.txt", PathUtils.normalize("a\\b/c.txt"));
        assertEquals("" + SEP + "a" + SEP + "b" + SEP + "c.txt",
                PathUtils.normalize("\\a\\b/c.txt"));
        assertEquals("C:" + SEP + "a" + SEP + "b" + SEP + "c.txt",
                PathUtils.normalize("C:\\a\\b/c.txt"));
        assertEquals("" + SEP + "" + SEP + "server" + SEP + "a" + SEP + "b" + SEP + "c.txt",
                PathUtils.normalize("\\\\server\\a\\b/c.txt"));
        assertEquals("~" + SEP + "a" + SEP + "b" + SEP + "c.txt",
                PathUtils.normalize("~\\a\\b/c.txt"));
        assertEquals("~user" + SEP + "a" + SEP + "b" + SEP + "c.txt",
                PathUtils.normalize("~user\\a\\b/c.txt"));

        assertEquals("a" + SEP + "c", PathUtils.normalize("a/b/../c"));
        assertEquals("c", PathUtils.normalize("a/b/../../c"));
        assertEquals("c" + SEP, PathUtils.normalize("a/b/../../c/"));
        assertNull(PathUtils.normalize("a/b/../../../c"));
        assertEquals("a" + SEP, PathUtils.normalize("a/b/.."));
        assertEquals("a" + SEP, PathUtils.normalize("a/b/../"));
        assertEquals("", PathUtils.normalize("a/b/../.."));
        assertEquals("", PathUtils.normalize("a/b/../../"));
        assertNull(PathUtils.normalize("a/b/../../.."));
        assertEquals("a" + SEP + "d", PathUtils.normalize("a/b/../c/../d"));
        assertEquals("a" + SEP + "d" + SEP, PathUtils.normalize("a/b/../c/../d/"));
        assertEquals("a" + SEP + "b" + SEP + "d", PathUtils.normalize("a/b//d"));
        assertEquals("a" + SEP + "b" + SEP, PathUtils.normalize("a/b/././."));
        assertEquals("a" + SEP + "b" + SEP, PathUtils.normalize("a/b/./././"));
        assertEquals("a" + SEP, PathUtils.normalize("./a/"));
        assertEquals("a", PathUtils.normalize("./a"));
        assertEquals("", PathUtils.normalize("./"));
        assertEquals("", PathUtils.normalize("."));
        assertNull(PathUtils.normalize("../a"));
        assertNull(PathUtils.normalize(".."));
        assertEquals("", PathUtils.normalize(""));

        assertEquals(SEP + "a", PathUtils.normalize("/a"));
        assertEquals(SEP + "a" + SEP, PathUtils.normalize("/a/"));
        assertEquals(SEP + "a" + SEP + "c", PathUtils.normalize("/a/b/../c"));
        assertEquals(SEP + "c", PathUtils.normalize("/a/b/../../c"));
        assertNull(PathUtils.normalize("/a/b/../../../c"));
        assertEquals(SEP + "a" + SEP, PathUtils.normalize("/a/b/.."));
        assertEquals(SEP + "", PathUtils.normalize("/a/b/../.."));
        assertNull(PathUtils.normalize("/a/b/../../.."));
        assertEquals(SEP + "a" + SEP + "d", PathUtils.normalize("/a/b/../c/../d"));
        assertEquals(SEP + "a" + SEP + "b" + SEP + "d", PathUtils.normalize("/a/b//d"));
        assertEquals(SEP + "a" + SEP + "b" + SEP, PathUtils.normalize("/a/b/././."));
        assertEquals(SEP + "a", PathUtils.normalize("/./a"));
        assertEquals(SEP + "", PathUtils.normalize("/./"));
        assertEquals(SEP + "", PathUtils.normalize("/."));
        assertNull(PathUtils.normalize("/../a"));
        assertNull(PathUtils.normalize("/.."));
        assertEquals(SEP + "", PathUtils.normalize("/"));

        assertEquals("~" + SEP + "a", PathUtils.normalize("~/a"));
        assertEquals("~" + SEP + "a" + SEP, PathUtils.normalize("~/a/"));
        assertEquals("~" + SEP + "a" + SEP + "c", PathUtils.normalize("~/a/b/../c"));
        assertEquals("~" + SEP + "c", PathUtils.normalize("~/a/b/../../c"));
        assertNull(PathUtils.normalize("~/a/b/../../../c"));
        assertEquals("~" + SEP + "a" + SEP, PathUtils.normalize("~/a/b/.."));
        assertEquals("~" + SEP + "", PathUtils.normalize("~/a/b/../.."));
        assertNull(PathUtils.normalize("~/a/b/../../.."));
        assertEquals("~" + SEP + "a" + SEP + "d", PathUtils.normalize("~/a/b/../c/../d"));
        assertEquals("~" + SEP + "a" + SEP + "b" + SEP + "d", PathUtils.normalize("~/a/b//d"));
        assertEquals("~" + SEP + "a" + SEP + "b" + SEP, PathUtils.normalize("~/a/b/././."));
        assertEquals("~" + SEP + "a", PathUtils.normalize("~/./a"));
        assertEquals("~" + SEP, PathUtils.normalize("~/./"));
        assertEquals("~" + SEP, PathUtils.normalize("~/."));
        assertNull(PathUtils.normalize("~/../a"));
        assertNull(PathUtils.normalize("~/.."));
        assertEquals("~" + SEP, PathUtils.normalize("~/"));
        assertEquals("~" + SEP, PathUtils.normalize("~"));

        assertEquals("~user" + SEP + "a", PathUtils.normalize("~user/a"));
        assertEquals("~user" + SEP + "a" + SEP, PathUtils.normalize("~user/a/"));
        assertEquals("~user" + SEP + "a" + SEP + "c", PathUtils.normalize("~user/a/b/../c"));
        assertEquals("~user" + SEP + "c", PathUtils.normalize("~user/a/b/../../c"));
        assertNull(PathUtils.normalize("~user/a/b/../../../c"));
        assertEquals("~user" + SEP + "a" + SEP, PathUtils.normalize("~user/a/b/.."));
        assertEquals("~user" + SEP + "", PathUtils.normalize("~user/a/b/../.."));
        assertNull(PathUtils.normalize("~user/a/b/../../.."));
        assertEquals("~user" + SEP + "a" + SEP + "d", PathUtils.normalize("~user/a/b/../c/../d"));
        assertEquals("~user" + SEP + "a" + SEP + "b" + SEP + "d",
                PathUtils.normalize("~user/a/b//d"));
        assertEquals("~user" + SEP + "a" + SEP + "b" + SEP, PathUtils.normalize("~user/a/b/././."));
        assertEquals("~user" + SEP + "a", PathUtils.normalize("~user/./a"));
        assertEquals("~user" + SEP + "", PathUtils.normalize("~user/./"));
        assertEquals("~user" + SEP + "", PathUtils.normalize("~user/."));
        assertNull(PathUtils.normalize("~user/../a"));
        assertNull(PathUtils.normalize("~user/.."));
        assertEquals("~user" + SEP, PathUtils.normalize("~user/"));
        assertEquals("~user" + SEP, PathUtils.normalize("~user"));

        assertEquals("C:" + SEP + "a", PathUtils.normalize("C:/a"));
        assertEquals("C:" + SEP + "a" + SEP, PathUtils.normalize("C:/a/"));
        assertEquals("C:" + SEP + "a" + SEP + "c", PathUtils.normalize("C:/a/b/../c"));
        assertEquals("C:" + SEP + "c", PathUtils.normalize("C:/a/b/../../c"));
        assertNull(PathUtils.normalize("C:/a/b/../../../c"));
        assertEquals("C:" + SEP + "a" + SEP, PathUtils.normalize("C:/a/b/.."));
        assertEquals("C:" + SEP + "", PathUtils.normalize("C:/a/b/../.."));
        assertNull(PathUtils.normalize("C:/a/b/../../.."));
        assertEquals("C:" + SEP + "a" + SEP + "d", PathUtils.normalize("C:/a/b/../c/../d"));
        assertEquals("C:" + SEP + "a" + SEP + "b" + SEP + "d", PathUtils.normalize("C:/a/b//d"));
        assertEquals("C:" + SEP + "a" + SEP + "b" + SEP, PathUtils.normalize("C:/a/b/././."));
        assertEquals("C:" + SEP + "a", PathUtils.normalize("C:/./a"));
        assertEquals("C:" + SEP + "", PathUtils.normalize("C:/./"));
        assertEquals("C:" + SEP + "", PathUtils.normalize("C:/."));
        assertNull(PathUtils.normalize("C:/../a"));
        assertNull(PathUtils.normalize("C:/.."));
        assertEquals("C:" + SEP + "", PathUtils.normalize("C:/"));

        assertEquals("C:" + "a", PathUtils.normalize("C:a"));
        assertEquals("C:" + "a" + SEP, PathUtils.normalize("C:a/"));
        assertEquals("C:" + "a" + SEP + "c", PathUtils.normalize("C:a/b/../c"));
        assertEquals("C:" + "c", PathUtils.normalize("C:a/b/../../c"));
        assertNull(PathUtils.normalize("C:a/b/../../../c"));
        assertEquals("C:" + "a" + SEP, PathUtils.normalize("C:a/b/.."));
        assertEquals("C:" + "", PathUtils.normalize("C:a/b/../.."));
        assertNull(PathUtils.normalize("C:a/b/../../.."));
        assertEquals("C:" + "a" + SEP + "d", PathUtils.normalize("C:a/b/../c/../d"));
        assertEquals("C:" + "a" + SEP + "b" + SEP + "d", PathUtils.normalize("C:a/b//d"));
        assertEquals("C:" + "a" + SEP + "b" + SEP, PathUtils.normalize("C:a/b/././."));
        assertEquals("C:" + "a", PathUtils.normalize("C:./a"));
        assertEquals("C:" + "", PathUtils.normalize("C:./"));
        assertEquals("C:" + "", PathUtils.normalize("C:."));
        assertNull(PathUtils.normalize("C:../a"));
        assertNull(PathUtils.normalize("C:.."));
        assertEquals("C:" + "", PathUtils.normalize("C:"));

        assertEquals(SEP + SEP + "server" + SEP + "a", PathUtils.normalize("//server/a"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP, PathUtils.normalize("//server/a/"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "c",
                PathUtils.normalize("//server/a/b/../c"));
        assertEquals(SEP + SEP + "server" + SEP + "c", PathUtils.normalize("//server/a/b/../../c"));
        assertNull(PathUtils.normalize("//server/a/b/../../../c"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP,
                PathUtils.normalize("//server/a/b/.."));
        assertEquals(SEP + SEP + "server" + SEP + "", PathUtils.normalize("//server/a/b/../.."));
        assertNull(PathUtils.normalize("//server/a/b/../../.."));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "d",
                PathUtils.normalize("//server/a/b/../c/../d"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "b" + SEP + "d",
                PathUtils.normalize("//server/a/b//d"));
        assertEquals(SEP + SEP + "server" + SEP + "a" + SEP + "b" + SEP,
                PathUtils.normalize("//server/a/b/././."));
        assertEquals(SEP + SEP + "server" + SEP + "a", PathUtils.normalize("//server/./a"));
        assertEquals(SEP + SEP + "server" + SEP + "", PathUtils.normalize("//server/./"));
        assertEquals(SEP + SEP + "server" + SEP + "", PathUtils.normalize("//server/."));
        assertNull(PathUtils.normalize("//server/../a"));
        assertNull(PathUtils.normalize("//server/.."));
        assertEquals(SEP + SEP + "server" + SEP + "", PathUtils.normalize("//server/"));
    }

    /**
     * Copied from {@link org.apache.commons.io.FilenameUtils#getName(String)} test cases.
     */
    @Test
    public void getName() {
        assertNull(PathUtils.getName(null));
        assertEquals("noseperator.inthispath", PathUtils.getName("noseperator.inthispath"));
        assertEquals("c.txt", PathUtils.getName("a/b/c.txt"));
        assertEquals("c", PathUtils.getName("a/b/c"));
        assertEquals("", PathUtils.getName("a/b/c/"));
        assertEquals("c", PathUtils.getName("a\\b\\c"));
    }

    /**
     * Copied from {@link org.apache.commons.io.FilenameUtils#getBaseName(String)} test cases.
     */
    @Test
    public void getBaseName() {
        assertNull(PathUtils.getBaseName(null));
        assertEquals("noseperator", PathUtils.getBaseName("noseperator.inthispath"));
        assertEquals("c", PathUtils.getBaseName("a/b/c.txt"));
        assertEquals("c", PathUtils.getBaseName("a/b/c"));
        assertEquals("", PathUtils.getBaseName("a/b/c/"));
        assertEquals("c", PathUtils.getBaseName("a\\b\\c"));
        assertEquals("file.txt", PathUtils.getBaseName("file.txt.bak"));
    }

    /**
     * Copied from {@link org.apache.commons.io.FilenameUtils#getFullPath(String)} test cases.
     */
    @Test
    public void getPath() {
        assertNull(PathUtils.getPath(null));
        assertEquals("", PathUtils.getPath("noseperator.inthispath"));
        assertEquals("a/b/", PathUtils.getPath("a/b/c.txt"));
        assertEquals("a/b/", PathUtils.getPath("a/b/c"));
        assertEquals("a/b/c/", PathUtils.getPath("a/b/c/"));
        assertEquals("a\\b\\", PathUtils.getPath("a\\b\\c"));

        assertNull(PathUtils.getPath(":"));
        assertNull(PathUtils.getPath("1:/a/b/c.txt"));
        assertNull(PathUtils.getPath("1:"));
        assertNull(PathUtils.getPath("1:a"));
        assertNull(PathUtils.getPath("///a/b/c.txt"));
        assertNull(PathUtils.getPath("//a"));

        assertEquals("", PathUtils.getPath(""));
        assertEquals("C:", PathUtils.getPath("C:"));
        assertEquals("C:/", PathUtils.getPath("C:/"));
        assertEquals("//server/", PathUtils.getPath("//server/"));
        assertEquals("~/", PathUtils.getPath("~"));
        assertEquals("~/", PathUtils.getPath("~/"));
        assertEquals("~user/", PathUtils.getPath("~user"));
        assertEquals("~user/", PathUtils.getPath("~user/"));

        assertEquals("a/b/", PathUtils.getPath("a/b/c.txt"));
        assertEquals("/a/b/", PathUtils.getPath("/a/b/c.txt"));
        assertEquals("C:", PathUtils.getPath("C:a"));
        assertEquals("C:a/b/", PathUtils.getPath("C:a/b/c.txt"));
        assertEquals("C:/a/b/", PathUtils.getPath("C:/a/b/c.txt"));
        assertEquals("//server/a/b/", PathUtils.getPath("//server/a/b/c.txt"));
        assertEquals("~/a/b/", PathUtils.getPath("~/a/b/c.txt"));
        assertEquals("~user/a/b/", PathUtils.getPath("~user/a/b/c.txt"));
    }

    /**
     * Copied from {@link org.apache.commons.io.FilenameUtils#concat(String, String)} test cases.
     */
    @Test
    public void join() {
        assertNull(PathUtils.join("", null));
        assertNull(PathUtils.join(null, null));
        assertNull(PathUtils.join(null, ""));
        assertNull(PathUtils.join(null, "a"));
        assertEquals(SEP + "a", PathUtils.join(null, "/a"));

        assertNull(PathUtils.join("", ":")); // invalid prefix
        assertNull(PathUtils.join(":", "")); // invalid prefix

        assertEquals("f" + SEP, PathUtils.join("", "f/"));
        assertEquals("f", PathUtils.join("", "f"));
        assertEquals("a" + SEP + "f" + SEP, PathUtils.join("a/", "f/"));
        assertEquals("a" + SEP + "f", PathUtils.join("a", "f"));
        assertEquals("a" + SEP + "b" + SEP + "f" + SEP, PathUtils.join("a/b/", "f/"));
        assertEquals("a" + SEP + "b" + SEP + "f", PathUtils.join("a/b", "f"));

        assertEquals("a" + SEP + "f" + SEP, PathUtils.join("a/b/", "../f/"));
        assertEquals("a" + SEP + "f", PathUtils.join("a/b", "../f"));
        assertEquals("a" + SEP + "c" + SEP + "g" + SEP, PathUtils.join("a/b/../c/", "f/../g/"));
        assertEquals("a" + SEP + "c" + SEP + "g", PathUtils.join("a/b/../c", "f/../g"));

        assertEquals("a" + SEP + "c.txt" + SEP + "f", PathUtils.join("a/c.txt", "f"));

        assertEquals(SEP + "f" + SEP, PathUtils.join("", "/f/"));
        assertEquals(SEP + "f", PathUtils.join("", "/f"));
        assertEquals(SEP + "f" + SEP, PathUtils.join("a/", "/f/"));
        assertEquals(SEP + "f", PathUtils.join("a", "/f"));

        assertEquals(SEP + "c" + SEP + "d", PathUtils.join("a/b/", "/c/d"));
        assertEquals("C:c" + SEP + "d", PathUtils.join("a/b/", "C:c/d"));
        assertEquals("C:" + SEP + "c" + SEP + "d", PathUtils.join("a/b/", "C:/c/d"));
        assertEquals("~" + SEP + "c" + SEP + "d", PathUtils.join("a/b/", "~/c/d"));
        assertEquals("~user" + SEP + "c" + SEP + "d", PathUtils.join("a/b/", "~user/c/d"));
        assertEquals("~" + SEP, PathUtils.join("a/b/", "~"));
        assertEquals("~user" + SEP, PathUtils.join("a/b/", "~user"));

        assertEquals("a" + SEP + "b" + SEP + "c", PathUtils.join("a//", "b///c"));
    }
}