package org.nexus_lab.relf.client.actions.standard;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFingerprintRequest;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFingerprintResponse;
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

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowLinux.class, ShadowPosix.class})
public class HashFileTest {
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
        request.setTuples(Collections.singletonList(tuple));

        HashFile action = new HashFile();
        action.execute(null, request, new ExecuteCallback());
    }

    public static class ExecuteCallback implements ActionCallback<RDFFingerprintResponse> {
        @Override
        public void onResponse(RDFFingerprintResponse fingerprint) {
            assertEquals("7f7d52a001b9d2c71b6bae1f189f41f3",
                    fingerprint.getHash().getMd5().getHexDigest());
            assertEquals("dea3c171abcdfb3e8380d6860630f618eb6e074f",
                    fingerprint.getHash().getSha1().getHexDigest());
            assertEquals("77d721c817f9d216c1fb783bcad9cdc20aaa2427402683f1f75dd6dfbe657470",
                    fingerprint.getHash().getSha256().getHexDigest());
            throw new TestSuccessException();
        }
    }
}