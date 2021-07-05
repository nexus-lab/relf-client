package org.nexus_lab.relf.client.actions.linux;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.content.Context;
import android.util.Log;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.actions.UsesPermission;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFFilesystem;
import org.nexus_lab.relf.lib.rdfvalues.protodict.RDFAttributedDict;
import org.nexus_lab.relf.service.RelfService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ruipeng Zhang
 */
@UsesPermission(READ_EXTERNAL_STORAGE)
public class EnumerateFilesystems implements Action<RDFNull, RDFFilesystem> {
    private static final String TAG = EnumerateFilesystems.class.getSimpleName();
    private static final String REGEX = "^([^\\s]+)(?:\\s+on)*\\s+([^\\s]+)(?:\\s+type)*\\s+([^\\s]+)"
            + "\\s+(?:\\()*([^\\s)]+)(?:(?:\\s+.*)|(?:\\)*))$";

    private InputStream readmount() throws IOException {
        try {
            return RelfService.open("/proc/mounts");
        } catch (IOException e) {
            Process process = Runtime.getRuntime().exec("/system/bin/mount");
            InputStream stream = process.getInputStream();
            try {
                process.waitFor();
            } catch (InterruptedException ignored) {
            }
            return stream;
        }
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFFilesystem> callback) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(readmount()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher match = Pattern.compile(REGEX).matcher(line.trim());
                if (match.matches() && match.groupCount() > 3) {
                    RDFFilesystem filesystem = new RDFFilesystem();
                    filesystem.setDevice(match.group(1));
                    filesystem.setMountPoint(match.group(2));
                    filesystem.setType(match.group(3));
                    String optionString = match.group(4);
                    RDFAttributedDict options = new RDFAttributedDict();
                    for (String option : optionString.split(",")) {
                        String[] keyValue = option.split("=");
                        options.put(keyValue[0], keyValue.length > 1 ? keyValue[1] : true);
                    }
                    filesystem.setOptions(options);
                    callback.onResponse(filesystem);
                } else {
                    Log.w(TAG, "\"" + line.trim() + "\" does not match regex \"" + REGEX + "\"");
                }
            }
            callback.onComplete();
        } catch (IOException e) {
            callback.onError(e);
        }
    }
}
