package org.nexus_lab.relf.lib.paths;

import org.nexus_lab.relf.lib.rdfvalues.client.ClientURN;
import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.proto.PathSpec;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RDFPathSpecTest {
    public RDFPathSpec buildPathSpec(String path, PathSpec.PathType type, RDFPathSpec nested) {
        RDFPathSpec pathspec = new RDFPathSpec();
        pathspec.setPathtype(type);
        if (path != null) {
            pathspec.setPath(path);
        }
        if (nested != null) {
            pathspec.setNestedPath(nested);
        }
        return pathspec;
    }

    public void assertPathEquals(String[] expected, RDFPathSpec actual) {
        for (int i = 0; i < actual.length(); i++) {
            assertEquals(expected[i], actual.get(i).getPath());
        }
    }

    @Test
    public void length() throws Exception {
        RDFPathSpec level3 = buildPathSpec("level3", PathSpec.PathType.TSK, null);
        RDFPathSpec level2 = buildPathSpec("level2", null, level3);
        RDFPathSpec level1 = buildPathSpec("level1", PathSpec.PathType.OS, level2);
        assertEquals(1, level1.length());
    }

    @Test
    public void get() throws Exception {
        RDFPathSpec level3 = buildPathSpec("level3", PathSpec.PathType.TSK, null);
        RDFPathSpec level2 = buildPathSpec("level2", PathSpec.PathType.OS, level3);
        RDFPathSpec level1 = buildPathSpec("level1", PathSpec.PathType.OS, level2);
        assertEquals(level3, level1.get(2));
    }

    @Test
    public void last() throws Exception {
        RDFPathSpec level3 = buildPathSpec("level3", PathSpec.PathType.TSK, null);
        RDFPathSpec level2 = buildPathSpec("level2", PathSpec.PathType.OS, level3);
        RDFPathSpec level1 = buildPathSpec("level1", PathSpec.PathType.OS, level2);
        assertEquals(level3, level1.last());
    }

    @Test
    public void insert() throws Exception {
        RDFPathSpec level3 = buildPathSpec("level3", PathSpec.PathType.TSK, null);
        RDFPathSpec level2 = buildPathSpec("level2", PathSpec.PathType.OS, null);
        RDFPathSpec level1 = buildPathSpec("level1", PathSpec.PathType.OS, level2);
        RDFPathSpec path1 = (RDFPathSpec) level1.duplicate();
        path1.insert(0, level3);
        assertPathEquals(new String[]{"level3", "level1", "level2"}, path1);
        RDFPathSpec path2 = (RDFPathSpec) level1.duplicate();
        path2.insert(1, level3);
        assertPathEquals(new String[]{"level1", "level3", "level2"}, path2);
        RDFPathSpec path3 = (RDFPathSpec) level1.duplicate();
        path3.insert(2, level3);
        assertPathEquals(new String[]{"level1", "level2", "level3"}, path3);
    }

    @Test
    public void append() throws Exception {
        RDFPathSpec level3 = buildPathSpec("level3", PathSpec.PathType.TSK, null);
        RDFPathSpec level2 = buildPathSpec("level2", PathSpec.PathType.OS, null);
        RDFPathSpec level1 = buildPathSpec("level1", PathSpec.PathType.OS, level2);
        RDFPathSpec path1 = (RDFPathSpec) level3.duplicate();
        path1.append(level1);
        assertPathEquals(new String[]{"level3", "level1", "level2"}, path1);
        RDFPathSpec path2 = (RDFPathSpec) level1.duplicate();
        path2.append(level3);
        assertPathEquals(new String[]{"level1", "level2", "level3"}, path2);
    }

    @Test
    public void collapse() throws Exception {
        RDFPathSpec level3 = buildPathSpec("/level3/", PathSpec.PathType.TSK, null);
        RDFPathSpec level2 = buildPathSpec("level2/", PathSpec.PathType.OS, level3);
        RDFPathSpec level1 = buildPathSpec("/level1", PathSpec.PathType.OS, level2);
        if (SystemUtils.IS_OS_WINDOWS) {
            assertEquals("\\level1\\level2\\level3\\", level1.collapse());
        } else {
            assertEquals("/level1/level2/level3/", level1.collapse());
        }
    }

    @Test
    public void remove() throws Exception {
        RDFPathSpec level4 = buildPathSpec("level4", PathSpec.PathType.TSK, null);
        RDFPathSpec level3 = buildPathSpec("level3", PathSpec.PathType.TSK, level4);
        RDFPathSpec level2 = buildPathSpec("level2", PathSpec.PathType.OS, level3);
        RDFPathSpec level1 = buildPathSpec("level1", PathSpec.PathType.OS, level2);
        assertPathEquals(new String[]{"level1"}, level1.remove(0));
        assertPathEquals(new String[]{"level2", "level3", "level4"}, level1);
        assertPathEquals(new String[]{"level3"}, level1.remove(1));
        assertPathEquals(new String[]{"level2", "level4"}, level1);
    }

    @Test
    public void directory() throws Exception {
        RDFPathSpec level3 = buildPathSpec("/level3", PathSpec.PathType.TSK, null);
        RDFPathSpec level2 = buildPathSpec("/level2", PathSpec.PathType.OS, level3);
        RDFPathSpec level1 = buildPathSpec("/level1/path", PathSpec.PathType.OS, level2);
        assertPathEquals(new String[]{"/level1/"}, level1.directory());
    }

    @Test
    public void basename() throws Exception {
        RDFPathSpec level3 = buildPathSpec("/level3", PathSpec.PathType.TSK, null);
        RDFPathSpec level2 = buildPathSpec("/level2", PathSpec.PathType.OS, level3);
        RDFPathSpec level1 = buildPathSpec("/level1/path", PathSpec.PathType.OS, level2);
        assertEquals("level3", level1.basename());
    }

    @Test
    public void aff4Path() throws Exception {
        RDFPathSpec level3 = buildPathSpec("/file", PathSpec.PathType.TSK, null);
        RDFPathSpec level2 = buildPathSpec("/windows/", PathSpec.PathType.TSK, level3);
        RDFPathSpec level1 = buildPathSpec("\\\\.\\Volume1\\", PathSpec.PathType.OS, level2);
        assertEquals("aff4:/C.81e760c9aec85ffd/fs/tsk/Volume1/windows/file",
                level1.aff4Path(new ClientURN("C.81e760c9aec85ffd")).stringify());
    }

}