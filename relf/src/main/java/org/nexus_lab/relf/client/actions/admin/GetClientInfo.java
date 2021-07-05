package org.nexus_lab.relf.client.actions.admin;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.config.ConfigLib;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.client.RDFClientInformation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Ruipeng Zhang
 */
public class GetClientInfo implements Action<RDFNull, RDFClientInformation> {
    /**
     * Get client information from bundled configurations
     *
     * @param context application context
     * @return client information in {@link RDFClientInformation}
     */
    public static RDFClientInformation getClientInfo(Context context) {
        Integer major = ConfigLib.get("Template.version_major", 3);
        Integer minor = ConfigLib.get("Template.version_minor", 2);
        Integer revision = ConfigLib.get("Template.version_revision", 0);
        Integer release = ConfigLib.get("Template.version_release", 0);
        int version = Integer.valueOf(
                String.format(Locale.getDefault(), "%d%d%d%d", major, minor, revision, release));

        RDFClientInformation info = new RDFClientInformation();
        info.setClientName(ConfigLib.get("Client.name", "ReLF"));
        info.setClientDescription(ConfigLib.get("Client.description", ""));
        info.setClientVersion(ConfigLib.get("Source.version_numeric", version));

        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), 0);
            ZipFile zipFile = new ZipFile(appInfo.sourceDir);
            ZipEntry classesEntry = zipFile.getEntry("classes.dex");
            String buildTime = SimpleDateFormat.getInstance().format(
                    new java.util.Date(classesEntry.getTime()));
            info.setBuildTime(ConfigLib.get("Client.build_time", buildTime));
            zipFile.close();
        } catch (Exception e) {
            info.setBuildTime(ConfigLib.get("Client.build_time", "Unknown"));
        }

        List<Object> labels = ConfigLib.get("Client.labels", new ArrayList<>());
        for (Object label : labels) {
            info.addLabel((String) label);
        }
        return info;
    }


    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFClientInformation> callback) {
        callback.onResponse(getClientInfo(context));
        callback.onComplete();
    }
}
