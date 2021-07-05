package org.nexus_lab.relf.client.vfs;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.proto.PathSpec;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * @author Ruipeng Zhang
 */
public class VFSTest {
    private RDFPathSpec fileSpec;

    @Before
    public void setup() {
        assumeTrue(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX);

        fileSpec = new RDFPathSpec();
        fileSpec.setPath(Constants.fspath("/tmp/vfs/Folder/FILE3"));
        fileSpec.setPathtype(PathSpec.PathType.OS);
    }

    @Test
    public void open() throws Exception {
        VFSFile file = VFS.open(fileSpec, null);
        assertEquals(Constants.fspath("/tmp/vfs/folder/file3"), file.getAbsolutePath());
    }
}