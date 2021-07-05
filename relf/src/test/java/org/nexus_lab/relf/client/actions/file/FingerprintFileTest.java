package org.nexus_lab.relf.client.actions.file;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.client.actions.standard.HashFileTest;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFingerprintRequest;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFingerprintTuple;
import org.nexus_lab.relf.lib.rdfvalues.paths.RDFPathSpec;
import org.nexus_lab.relf.proto.FingerprintTuple;
import org.nexus_lab.relf.proto.PathSpec;
import org.nexus_lab.relf.robolectric.ShadowLinux;
import org.nexus_lab.relf.robolectric.ShadowPosix;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowLinux.class, ShadowPosix.class})
public class FingerprintFileTest {
    private RDFPathSpec pathspec;

    @Before
    public void setup() {
        pathspec = new RDFPathSpec();
        pathspec.setPath(Constants.fspath("/tmp/vfs/file1"));
        // Remove this when VFS code is refactored
        if (SystemUtils.IS_OS_WINDOWS) {
            pathspec.setPath("\\" + pathspec.getPath());
        }
        pathspec.setPathtype(PathSpec.PathType.OS);
    }

    @Test(expected = TestSuccessException.class)
    public void execute() {
        RDFFingerprintRequest request = new RDFFingerprintRequest();
        request.setMaxFilesize(Long.MAX_VALUE);
        request.setPathspec(pathspec);
        RDFFingerprintTuple tuple = new RDFFingerprintTuple();
        tuple.setHashers(Arrays.asList(
                FingerprintTuple.HashType.MD5,
                FingerprintTuple.HashType.SHA1,
                FingerprintTuple.HashType.SHA256));
        tuple.setFpType(FingerprintTuple.Type.FPT_GENERIC);
        request.setTuples(Collections.singletonList(tuple));

        FingerprintFile action = new FingerprintFile();
        action.execute(null, request, new HashFileTest.ExecuteCallback());
    }

}