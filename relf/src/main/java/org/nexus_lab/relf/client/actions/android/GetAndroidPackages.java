package org.nexus_lab.relf.client.actions.android;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import org.nexus_lab.relf.client.actions.Action;
import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.lib.rdfvalues.RDFDatetime;
import org.nexus_lab.relf.lib.rdfvalues.RDFNull;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidPackageInfo;
import org.nexus_lab.relf.lib.rdfvalues.crypto.RDFX509Cert;
import org.nexus_lab.relf.proto.AndroidPackageInfo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ruipeng Zhang
 */
public class GetAndroidPackages implements Action<RDFNull, RDFAndroidPackageInfo> {
    private RDFX509Cert createRDFSignature(byte[] rawBytes) {
        InputStream input = new ByteArrayInputStream(rawBytes);
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(input);
            RDFX509Cert result = new RDFX509Cert();
            result.setValue(certificate);
            return result;
        } catch (CertificateException e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private RDFAndroidPackageInfo createAndroidPackageInfo(PackageManager pm,
            PackageInfo packageInfo) {
        ApplicationInfo appInfo = packageInfo.applicationInfo;
        RDFAndroidPackageInfo pkg = new RDFAndroidPackageInfo();
        pkg.setIsEnabled(appInfo.enabled);
        pkg.setName(pm.getApplicationLabel(appInfo).toString());
        pkg.setFirstInstallTime(new RDFDatetime(packageInfo.firstInstallTime * 1000));
        pkg.setInstallLocation(
                AndroidPackageInfo.InstallLocation.forNumber(packageInfo.installLocation));
        pkg.setLastUpdateTime(new RDFDatetime(packageInfo.lastUpdateTime * 1000));
        pkg.setPackageName(packageInfo.packageName);
        pkg.setSourceDir(appInfo.sourceDir);
        pkg.setTargetSdkVersion(appInfo.targetSdkVersion);
        pkg.setUid(appInfo.uid);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            //noinspection deprecation
            pkg.setVersionCode((long) packageInfo.versionCode);
        } else {
            pkg.setVersionCode(packageInfo.getLongVersionCode());
        }

        if (appInfo.dataDir != null) {
            pkg.setDataDir(appInfo.dataDir);
        }
        if (packageInfo.versionName != null) {
            pkg.setVersionName(packageInfo.versionName);
        }
        if (packageInfo.sharedUserId != null) {
            pkg.setSharedUserId(packageInfo.sharedUserId);
        }
        if (packageInfo.requestedPermissions != null) {
            pkg.setRequestedPermissions(Arrays.asList(packageInfo.requestedPermissions));
        }
        if (packageInfo.gids != null) {
            for (int gid : packageInfo.gids) {
                pkg.addGid(gid);
            }
        }
        Signature[] signatures = new Signature[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (packageInfo.signingInfo != null
                    && packageInfo.signingInfo.getApkContentsSigners() != null) {
                signatures = packageInfo.signingInfo.getApkContentsSigners();
            }
        } else if (packageInfo.signatures != null) {
            signatures = packageInfo.signatures;
        }
        for (Signature signature : signatures) {
            RDFX509Cert cert = createRDFSignature(signature.toByteArray());
            if (cert != null) {
                pkg.addSignature(cert);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pkg.setMinSdkVersion(appInfo.minSdkVersion);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pkg.setCategory(AndroidPackageInfo.Category.forNumber(appInfo.category));
        }
        return pkg;
    }

    @Override
    public void execute(Context context, RDFNull request, ActionCallback<RDFAndroidPackageInfo> callback) {
        PackageManager manager = context.getPackageManager();
        if (manager == null) {
            throw new NullPointerException("Cannot access Android package manager");
        }
        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            flags = PackageManager.GET_GIDS | PackageManager.GET_SIGNING_CERTIFICATES
                    | PackageManager.GET_META_DATA;
        } else {
            //noinspection deprecation
            flags = PackageManager.GET_GIDS | PackageManager.GET_SIGNATURES
                    | PackageManager.GET_META_DATA;
        }
        List<PackageInfo> packages = manager.getInstalledPackages(flags);

        for (PackageInfo packageInfo : packages) {
            callback.onResponse(createAndroidPackageInfo(manager, packageInfo));
        }
        callback.onComplete();
    }
}
