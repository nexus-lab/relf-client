package org.nexus_lab.relf.client.actions.standard;

import android.content.Context;
import android.os.SystemClock;
import android.system.Os;
import android.system.OsConstants;
import android.util.Pair;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.Constants;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.network.Netstat;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFProcess;
import org.nexus_lab.relf.robolectric.ShadowRelfService;
import org.nexus_lab.relf.robolectric.ShadowLinux;
import org.nexus_lab.relf.robolectric.ShadowOs;
import org.nexus_lab.relf.robolectric.ShadowPosix;
import org.nexus_lab.relf.utils.os.Process;

import org.apache.commons.lang3.SystemUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assume.assumeTrue;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowPosix.class, ShadowLinux.class, ShadowRelfService.class, ShadowOs.class})
public class ListProcessesTest {
    @BeforeClass
    public static void setup() {
        assumeTrue(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX);
        Constants.overrideOsConstants();
    }

    @Test(expected = TestSuccessException.class)
    public void execute() {
        Context context = ApplicationProvider.getApplicationContext();
        new ListProcesses().execute(context, null, new ActionCallback<RDFProcess>() {
            private int i;

            @Override
            public void onResponse(RDFProcess response) {
                long pageSize = Os.sysconf(OsConstants._SC_PAGESIZE);
                long clockTick = Os.sysconf(OsConstants._SC_CLK_TCK);

                Process process = new Process(response.getPid());
                assertEquals(process.name(), response.getName());
                assertArrayEquals(process.cmdline(), response.getCmdline().toArray(new String[0]));
                assertEquals(process.cwd(), response.getCwd());
                assertEquals(process.exe(), response.getExe());
                assertEquals(process.username(), response.getUsername());
                assertEquals(process.terminal(), response.getTerminal());
                assertEquals(process.stat().ppid(), response.getPpid().intValue());
                assertEquals(process.stat().nice(), response.getNice().intValue());
                assertEquals(process.statm().size() * pageSize, response.getVmsSize().longValue());
                assertEquals(process.statm().resident() * pageSize, response.getRssSize().longValue());
                assertNotEquals("?", response.getStatus());
                assertEquals(process.stat().stime() / (float) clockTick, response.getSystemCpuTime(), 0);
                assertEquals(process.stat().utime() / (float) clockTick, response.getUserCpuTime(), 0);
                assertArrayEquals(process.status().uids(), new int[]{
                        response.getRealUid(),
                        response.getEffectiveUid(),
                        response.getSavedUid()
                });
                assertArrayEquals(process.status().gids(), new int[]{
                        response.getRealGid(),
                        response.getEffectiveGid(),
                        response.getSavedGid()
                });
                assertEquals(SystemClock.elapsedRealtime() + (long) (process.stat().starttime()
                        / clockTick * 1e6), response.getCtime().longValue());

                Pair<Integer, String>[] fds = process.fd();
                assertEquals(fds.length, response.getOpenFiles().size());
                for (int j = 0; j < response.getOpenFiles().size(); j++) {
                    assertEquals(fds[j].second, response.getOpenFiles().get(j));
                }
                assertEquals(Netstat.getInetConnections(process).size(), response.getConnections().size());
                i++;
            }

            @Override
            public void onComplete() {
                assertEquals(2, i);
                throw new TestSuccessException();
            }
        });
    }
}