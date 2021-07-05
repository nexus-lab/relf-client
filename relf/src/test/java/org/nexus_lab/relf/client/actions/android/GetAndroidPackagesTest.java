package org.nexus_lab.relf.client.actions.android;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.SigningInfo;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.nexus_lab.relf.client.actions.ActionCallback;
import org.nexus_lab.relf.client.exceptions.TestSuccessException;
import org.nexus_lab.relf.lib.rdfvalues.android.RDFAndroidPackageInfo;
import org.nexus_lab.relf.utils.RandomUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowPackageManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Ruipeng Zhang
 */
@RunWith(RobolectricTestRunner.class)
public class GetAndroidPackagesTest {
    private Context context;
    private Random random;
    private Map<String, PackageInfo> packages;
    private final ActionCallback<RDFAndroidPackageInfo> callback =
            new ActionCallback<RDFAndroidPackageInfo>() {
                private int i;

                @Override
                public void onResponse(RDFAndroidPackageInfo pkg) {
                    PackageInfo info = packages.get(pkg.getPackageName());
                    assertNotNull(info);
                    ApplicationInfo ai = info.applicationInfo;
                    assertEquals(ai.uid, pkg.getUid().longValue());
                    assertEquals(ai.enabled, pkg.getIsEnabled());
                    assertEquals(context.getPackageManager().getApplicationLabel(ai),
                            pkg.getName());
                    assertEquals(ai.dataDir, pkg.getDataDir());
                    assertEquals(ai.sourceDir, pkg.getSourceDir());
                    assertEquals(ai.targetSdkVersion, pkg.getTargetSdkVersion().longValue());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        assertEquals(ai.minSdkVersion, pkg.getMinSdkVersion().longValue());
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        assertEquals(ai.category, pkg.getCategory().getNumber());
                    }
                    assertEquals(info.firstInstallTime * 1000,
                            pkg.getFirstInstallTime().asMicrosecondsFromEpoch());
                    assertEquals(info.lastUpdateTime * 1000,
                            pkg.getLastUpdateTime().asMicrosecondsFromEpoch());
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        assertEquals(info.versionCode, pkg.getVersionCode().longValue());
                    } else {
                        assertEquals(info.getLongVersionCode(), pkg.getVersionCode().longValue());
                    }
                    assertEquals(info.versionName, pkg.getVersionName());
                    assertEquals(info.packageName, pkg.getPackageName());
                    assertArrayEquals(info.requestedPermissions,
                            pkg.getRequestedPermissions().toArray());
                    assertArrayEquals(Arrays.stream(info.gids).boxed().toArray(Integer[]::new),
                            pkg.getGids().toArray());
                    // Signatures not tested
                    i++;
                }

                @Override
                public void onComplete() {
                    assertEquals(packages.size(), i);
                    throw new TestSuccessException();
                }
            };

    private PackageInfo createPackage() {
        PackageInfo info = new PackageInfo();
        ApplicationInfo appInfo = new ApplicationInfo();
        info.applicationInfo = appInfo;
        appInfo.uid = random.nextInt();
        appInfo.enabled = random.nextBoolean();
        appInfo.packageName = RandomStringUtils.randomAlphanumeric(10);
        appInfo.labelRes = android.R.string.unknownName;
        appInfo.dataDir = "/data/" + RandomStringUtils.randomAlphanumeric(10);
        appInfo.sourceDir = "/data/" + RandomStringUtils.randomAlphanumeric(10);
        appInfo.targetSdkVersion = RandomUtils.oneOf(
                Build.VERSION_CODES.LOLLIPOP,
                Build.VERSION_CODES.LOLLIPOP_MR1,
                Build.VERSION_CODES.M,
                Build.VERSION_CODES.N,
                Build.VERSION_CODES.N_MR1,
                Build.VERSION_CODES.O,
                Build.VERSION_CODES.O_MR1,
                Build.VERSION_CODES.P
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appInfo.minSdkVersion = RandomUtils.oneOf(
                    Build.VERSION_CODES.LOLLIPOP,
                    Build.VERSION_CODES.LOLLIPOP_MR1,
                    Build.VERSION_CODES.M,
                    Build.VERSION_CODES.N,
                    Build.VERSION_CODES.N_MR1,
                    Build.VERSION_CODES.O,
                    Build.VERSION_CODES.O_MR1,
                    Build.VERSION_CODES.P
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appInfo.category = RandomUtils.oneOf(
                    ApplicationInfo.CATEGORY_UNDEFINED,
                    ApplicationInfo.CATEGORY_GAME,
                    ApplicationInfo.CATEGORY_AUDIO,
                    ApplicationInfo.CATEGORY_VIDEO,
                    ApplicationInfo.CATEGORY_IMAGE,
                    ApplicationInfo.CATEGORY_SOCIAL,
                    ApplicationInfo.CATEGORY_NEWS,
                    ApplicationInfo.CATEGORY_MAPS,
                    ApplicationInfo.CATEGORY_PRODUCTIVITY
            );
        }
        info.firstInstallTime = ((Double) (Math.max(1,
                (double) random.nextLong() / System.currentTimeMillis())
                * System.currentTimeMillis())).longValue();
        info.lastUpdateTime = Math.max(info.firstInstallTime + random.nextInt(),
                System.currentTimeMillis());
        info.installLocation = RandomUtils.oneOf(
                PackageInfo.INSTALL_LOCATION_AUTO,
                PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY,
                PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL
        );
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            //noinspection deprecation
            info.versionCode = random.nextInt();
        } else {
            info.setLongVersionCode(random.nextInt());
        }
        info.versionName = RandomStringUtils.randomAlphanumeric(10);
        info.packageName = appInfo.packageName;
        info.requestedPermissions = new String[]{
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.BATTERY_STATS
        };
        info.gids = new int[]{random.nextInt(), random.nextInt(), random.nextInt()};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo = new SigningInfo();
        }
        return info;
    }

    @Before
    public void setup() {
        random = new Random();
        packages = new HashMap<>();
        context = ApplicationProvider.getApplicationContext();
        PackageManager manager = context.getPackageManager();
        List<PackageInfo> installedPackages = manager.getInstalledPackages(PackageManager.GET_GIDS
                | PackageManager.GET_SIGNING_CERTIFICATES | PackageManager.GET_META_DATA);
        for (PackageInfo packageInfo : installedPackages) {
            packages.put(packageInfo.packageName, packageInfo);
        }
        ShadowPackageManager shadowPackageManager = Shadows.shadowOf(manager);
        for (int i = 0; i < 5; i++) {
            PackageInfo packageInfo = createPackage();
            shadowPackageManager.addPackage(packageInfo);
            packages.put(packageInfo.packageName, packageInfo);
        }
    }

    @Test(expected = TestSuccessException.class)
    public void execute() {
        new GetAndroidPackages().execute(context, null, callback);
    }

}