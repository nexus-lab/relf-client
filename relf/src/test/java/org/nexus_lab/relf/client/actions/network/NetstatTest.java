package org.nexus_lab.relf.client.actions.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFNetworkConnection;
import org.nexus_lab.relf.proto.NetworkConnection;
import org.nexus_lab.relf.robolectric.ShadowLinux;
import org.nexus_lab.relf.robolectric.ShadowPosix;

import org.apache.commons.lang3.SystemUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nexus_lab.relf.robolectric.ShadowRelfService;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowPosix.class, ShadowLinux.class, ShadowRelfService.class})
public class NetstatTest {
    @BeforeClass
    public static void setup() {
        assumeTrue(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX);
        Constants.overrideOsConstants();
    }

    @Test(expected = TestSuccessException.class)
    public void execute() {
        Context context = ApplicationProvider.getApplicationContext();
        new Netstat().execute(context, null, new ActionCallback<RDFNetworkConnection>() {
            private int i;

            @Override
            public void onResponse(RDFNetworkConnection connection) {
                switch (connection.getLocalAddress().getPort()) {
                    case 38793:
                        assertEquals(9999, connection.getPid().intValue());
                        assertEquals("relf_service", connection.getProcessName());
                        assertEquals("192.168.29.205", connection.getLocalAddress().getIp());
                        assertEquals("192.168.29.204", connection.getRemoteAddress().getIp());
                        assertEquals(8080, connection.getRemoteAddress().getPort());
                        assertEquals(NetworkConnection.State.CLOSE_WAIT, connection.getState());
                        assertEquals(NetworkConnection.Type.SOCK_STREAM, connection.getType());
                        break;
                    case 63342:
                    case 38885:
                        break;
                    default:
                        throw new RuntimeException("Local port out of range");
                }
                i++;
            }

            @Override
            public void onComplete() {
                assertEquals(3, i);
                throw new TestSuccessException();
            }
        });
    }
}