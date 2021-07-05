package org.nexus_lab.relf.client.actions.standard;

import android.content.Context;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFExecuteRequest;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFExecuteResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Ruipeng Zhang
 */
public class ExecuteCommand implements Action<RDFExecuteRequest, RDFExecuteResponse> {
    private static void waitFor(Process process, long timeout, TimeUnit unit)
            throws InterruptedException {
        long startTime = System.nanoTime();
        long remain = unit.toNanos(timeout);
        do {
            try {
                process.exitValue();
                return;
            } catch (IllegalThreadStateException e) {
                if (remain > 0) {
                    Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(remain) + 1, 100));
                }
            }
            remain = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        } while (remain > 0 || timeout == 0);
    }

    private static Thread getOutputAsync(InputStream stream, Writer writer) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try (Reader reader = new BufferedReader(new InputStreamReader(stream))) {
                    int c;
                    while (!isInterrupted()) {
                        c = reader.read();
                        if (c != -1) {
                            writer.append((char) c);
                        } else {
                            break;
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        };
        thread.start();
        return thread;
    }

    @Override
    public void execute(Context context, RDFExecuteRequest request, ActionCallback<RDFExecuteResponse> callback) {
        RDFExecuteResponse response = new RDFExecuteResponse();
        StringWriter stdout = new StringWriter();
        StringWriter stderr = new StringWriter();
        List<String> command = new ArrayList<>();
        command.add(request.getCmd());
        command.addAll(request.getArgs());
        try {
            long startTime = System.nanoTime();
            Process process = new ProcessBuilder(command).start();
            Thread threadStdout = getOutputAsync(process.getInputStream(), stdout);
            Thread threadStderr = getOutputAsync(process.getErrorStream(), stderr);
            waitFor(process, request.getTimeLimit() == null ? 0 : request.getTimeLimit(),
                    TimeUnit.SECONDS);
            int timeUsed = (int) ((System.nanoTime() - startTime) / 1000000);
            Thread.sleep(100);
            threadStdout.interrupt();
            threadStderr.interrupt();
            process.destroy();
            response.setRequest(request);
            response.setStdout(stdout.toString().getBytes());
            response.setStderr(stderr.toString().getBytes());
            stdout.close();
            stderr.close();
            try {
                response.setExitStatus(process.exitValue());
            } catch (IllegalThreadStateException ignored) {
                response.setExitStatus(Integer.MIN_VALUE);
            }
            response.setTimeUsed(timeUsed);
            callback.onResponse(response);
            callback.onComplete();
        } catch (IOException | InterruptedException e) {
            callback.onError(e);
        }
    }
}
