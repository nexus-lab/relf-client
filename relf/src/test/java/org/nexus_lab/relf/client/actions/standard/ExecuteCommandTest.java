package org.nexus_lab.relf.client.actions.standard;

import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFExecuteRequest;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFExecuteResponse;

import org.apache.commons.lang3.SystemUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class ExecuteCommandTest {
    private final ActionCallback<RDFExecuteResponse> callback = response -> {
        switch (response.getRequest().getCmd()) {
            case "echo":
                assertArrayEquals("hello world\n".getBytes(), response.getStdout());
                assertEquals(0, response.getStderr().length);
                assertEquals(0, response.getExitStatus().intValue());
                assertTrue(response.getTimeUsed() < 1000);
                break;
            case "cat":
                assertEquals(0, response.getStdout().length);
                assertTrue(new String(response.getStderr()).contains("no_such_file_"));
                assertEquals(1, response.getExitStatus().intValue());
                assertTrue(response.getTimeUsed() < 1000);
                break;
            case "sleep":
                assertEquals(0, response.getStdout().length);
                assertEquals(0, response.getStderr().length);
                assertEquals(Integer.MIN_VALUE, response.getExitStatus().intValue());
                assertTrue(response.getTimeUsed() > 1000);
                assertTrue(response.getTimeUsed() < 2000);
                break;
            default:
        }
        throw new TestSuccessException();
    };

    @BeforeClass
    public static void setup() {
        assumeTrue(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX);
    }

    @Test(expected = TestSuccessException.class)
    public void execute_Echo() {
        RDFExecuteRequest request = new RDFExecuteRequest();
        request.setCmd("echo");
        request.setArgs(Collections.singletonList("hello world"));
        new ExecuteCommand().execute(null, request, callback);
    }

    @Test(expected = TestSuccessException.class)
    public void execute_Cat() {
        RDFExecuteRequest request = new RDFExecuteRequest();
        request.setCmd("cat");
        request.setArgs(Collections.singletonList("no_such_file_" + new Random().nextInt()));
        new ExecuteCommand().execute(null, request, callback);
    }

    @Test(expected = TestSuccessException.class)
    public void execute_Sleep() {
        RDFExecuteRequest request = new RDFExecuteRequest();
        request.setCmd("sleep");
        request.setArgs(Collections.singletonList("2"));
        request.setTimeLimit(1);
        new ExecuteCommand().execute(null, request, callback);
    }
}