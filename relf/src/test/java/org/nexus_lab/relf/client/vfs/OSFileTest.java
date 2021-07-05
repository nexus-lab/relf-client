package org.nexus_lab.relf.client.vfs;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.proto.PathSpec;
import org.nexus_lab.relf.robolectric.ShadowLinux;
import org.nexus_lab.relf.robolectric.ShadowPosix;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowLinux.class, ShadowPosix.class})
public class OSFileTest {
    private OSFile folder;
    private OSFile file;

    @Before
    public void setup() {
        RDFPathSpec folderSpec = new RDFPathSpec();
        folderSpec.setPath(Constants.fspath("/tmp/vfs"));
        folderSpec.setPathtype(PathSpec.PathType.OS);
        folder = new OSFile(null, folderSpec, null);

        RDFPathSpec fileSpec = new RDFPathSpec();
        fileSpec.setPath("file1");
        fileSpec.setPathtype(PathSpec.PathType.OS);
        file = new OSFile(folder, fileSpec, null);
    }

    @Test
    public void list() {
        assertEquals(new HashSet<>(Arrays.asList("folder", "file1", "file2")),
                new HashSet<>(Arrays.asList(folder.list())));
    }

    @Test
    public void read() throws Exception {
        assertEquals("abcdef", new String(file.read(6)));
        file.seek(6, 0);
        assertEquals(6, file.tell());
        assertEquals("ghijkl", new String(file.read(6)));
        file.seek(6, 1);
        assertEquals(18, file.tell());
        assertEquals("stuvwx", new String(file.read(6)));
        file.seek(6, 2);
        assertEquals(42, file.tell());
        assertEquals(null, file.read(6));
    }

    @Test
    public void isDirectory() {
        assertTrue(folder.isDirectory());
    }

    @Test
    public void listFiles() {
        Set<String> filenames = new HashSet<>();
        for (VFSFile file : folder.listFiles()) {
            filenames.add(file.getName());
        }
        assertEquals(new HashSet<>(Arrays.asList("folder", "file1", "file2")), filenames);
    }

    @Test
    public void getName() {
        assertEquals("vfs", folder.getName());
    }
}